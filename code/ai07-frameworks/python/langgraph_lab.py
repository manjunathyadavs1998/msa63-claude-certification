"""AI07 L2: real LangGraph running with no model at all. The "agent" node is plain Python,
so this shows state, checkpoints and interrupts, and says nothing about model quality."""
from typing import TypedDict

from langgraph.checkpoint.memory import InMemorySaver
from langgraph.graph import END, START, StateGraph
from langgraph.types import Command, interrupt

entries = {"check": 0, "approve": 0}
refunds_sent = []


class Case(TypedDict, total=False):
    order: str
    amount: int
    decision: str
    result: str


def check_policy(state: Case):
    entries["check"] += 1
    return {"decision": "needs_human" if state["amount"] > 500 else "auto"}


def route(state: Case):
    return "approve" if state["decision"] == "needs_human" else "refund"


def approve(state: Case):
    entries["approve"] += 1
    print(f"  approve node entered, time {entries['approve']}")
    answer = interrupt({"question": f"Refund ${state['amount']}?"})
    return {"decision": "approved" if answer else "rejected"}


def refund(state: Case):
    if state["decision"] == "rejected":
        return {"result": "not refunded"}
    refunds_sent.append(state["order"])
    return {"result": f"refunded {state['order']}"}


builder = StateGraph(Case)
builder.add_node("check", check_policy)
builder.add_node("approve", approve)
builder.add_node("refund", refund)
builder.add_edge(START, "check")
builder.add_conditional_edges("check", route, ["approve", "refund"])
builder.add_edge("approve", "refund")
builder.add_edge("refund", END)
graph = builder.compile(checkpointer=InMemorySaver())

print("1. Small refund: no pause")
small = {"configurable": {"thread_id": "case-1"}}
print(" ", graph.invoke({"order": "7781", "amount": 40}, small)["result"])

print("2. Large refund: the graph pauses and returns control")
large = {"configurable": {"thread_id": "case-2"}}
paused = graph.invoke({"order": "8120", "amount": 900}, large)
print("  paused with:", paused["__interrupt__"][0].value)
print("  next node:", graph.get_state(large).next)
print("  refunds so far:", refunds_sent)

print("3. Resume, on the same thread, hours later or in another process")
print(" ", graph.invoke(Command(resume=True), large)["result"])
print("  check node entries:", entries["check"], "(2 cases, so it didn't re-run on resume)")
print("  approve node entries:", entries["approve"], "(restarted from its first line)")

print("4. A new thread_id knows nothing")
other = {"configurable": {"thread_id": "case-3"}}
print("  next node on an unknown thread:", graph.get_state(other).next)

print("5. Checkpoints saved for case-2:", len(list(graph.get_state_history(large))))
