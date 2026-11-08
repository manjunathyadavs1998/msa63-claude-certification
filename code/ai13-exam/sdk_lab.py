"""Builds Agent SDK options and runs hook callbacks directly. Calls no model and needs no key."""
import asyncio
import dataclasses
from datetime import datetime, timezone

from claude_agent_sdk import AgentDefinition, ClaudeAgentOptions, HookMatcher

REFUND_LIMIT = 500


async def refund_gate(input_data, tool_use_id, context):
    """PreToolUse: blocks a refund above the limit and tells the model where to go instead."""
    amount = input_data["tool_input"].get("amount", 0)
    if amount > REFUND_LIMIT:
        return {
            "hookSpecificOutput": {
                "hookEventName": input_data["hook_event_name"],
                "permissionDecision": "deny",
                "permissionDecisionReason":
                    f"Refunds above ${REFUND_LIMIT} go to a human: call escalate_to_human.",
            }
        }
    return {}


def to_iso(value):
    """Turns a Unix timestamp or an ISO 8601 string into one UTC ISO 8601 form."""
    if isinstance(value, (int, float)):
        moment = datetime.fromtimestamp(value, tz=timezone.utc)
    else:
        moment = datetime.fromisoformat(value.replace("Z", "+00:00")).astimezone(timezone.utc)
    return moment.strftime("%Y-%m-%dT%H:%M:%SZ")


STATUS = {1: "pending", 2: "shipped", 3: "delivered"}


async def normalize_order(input_data, tool_use_id, context):
    """PostToolUse: replaces the tool's output, so the model only ever sees one format."""
    raw = input_data["tool_response"]
    return {
        "hookSpecificOutput": {
            "hookEventName": input_data["hook_event_name"],
            "updatedToolOutput": {
                "order_id": raw["order_id"],
                "placed_at": to_iso(raw["placed_at"]),
                "status": STATUS.get(raw["status"], raw["status"]),
            },
        }
    }


def build_options():
    return ClaudeAgentOptions(
        allowed_tools=["Agent", "mcp__orders__lookup_order"],
        agents={
            "order-investigator": AgentDefinition(
                description="Investigates one order. Use for each separate order in a request.",
                prompt="You investigate one order. Report what you tried and what you found.",
                tools=["mcp__orders__lookup_order"],
            )
        },
        hooks={
            "PreToolUse": [HookMatcher(matcher="mcp__orders__process_refund",
                                       hooks=[refund_gate], on_failure="block")],
            "PostToolUse": [HookMatcher(matcher="mcp__orders__lookup_order",
                                        hooks=[normalize_order])],
        },
        max_turns=20,
    )


async def main():
    options = build_options()
    print("allowed_tools:", options.allowed_tools)
    print("subagent tools:", options.agents["order-investigator"].tools)
    names = {f.name for f in dataclasses.fields(ClaudeAgentOptions)}
    for name in ["resume", "fork_session", "continue_conversation", "max_turns",
                 "max_budget_usd", "permission_mode", "mcp_servers", "system_prompt"]:
        print(f"option {name}: {'present' if name in names else 'MISSING'}")
    base = {"hook_event_name": "PreToolUse", "tool_name": "mcp__orders__process_refund"}
    for amount in (120, 800):
        out = await refund_gate({**base, "tool_input": {"amount": amount}}, "t1", None)
        decision = out.get("hookSpecificOutput", {}).get("permissionDecision", "no objection")
        print(f"refund {amount}: {decision}")
    for raw in ({"order_id": "A1", "placed_at": 1767225600, "status": 2},
                {"order_id": "B2", "placed_at": "2026-01-01T05:30:00+05:30", "status": 3}):
        out = await normalize_order({"hook_event_name": "PostToolUse", "tool_response": raw},
                                    "t2", None)
        print("normalized:", out["hookSpecificOutput"]["updatedToolOutput"])


asyncio.run(main())
