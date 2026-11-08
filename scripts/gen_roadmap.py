"""Generates 00-planning/lessons.json and COURSE_ROADMAP.md, so every total is computed.

Edit MODULES here, then run: python -I -X utf8 scripts/gen_roadmap.py
Each lesson is (title, page budget, tag). Tags: "" normal, "DP" ten-part design problem,
"NEW-CERT" certification-only material, "PILOT" already drafted as the format pilot.
"""
import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
WORDS_PER_PAGE = 215  # measured by the HLD and LLD courses

MODULES = [
    ("I", "AI01", "AI and Machine Learning Fundamentals", "Support",
     "Your first topic. Builds the vocabulary everything else uses.", [
        ("What Learning From Data Means", 22, ""),
        ("Classical ML and How to Measure It", 28, ""),
        ("Neural Networks and How They Train", 28, ""),
        ("From Word Counts to Word Vectors", 22, ""),
     ]),
    ("I", "AI02", "Generative AI and LLM Fundamentals", "Support",
     "Your second topic. Why LLMs behave as they do, and how Anthropic approaches training.", [
        ("Tokens and Tokenization", 20, ""),
        ("Attention From First Principles", 28, ""),
        ("The Transformer End to End", 28, ""),
        ("How an LLM Is Trained: RLHF and Constitutional AI", 28, ""),
        ("Inference and Model Serving Internals", 30, ""),
        ("Why LLMs Fail: Hallucination, Position and Interpretability", 26, ""),
        ("Generative AI Beyond Text and Choosing a Model", 20, ""),
     ]),
    ("I", "AI03", "Working With Claude: API, Prompting and Context", "Core",
     "Added: not in your list, but RAG, agents and the exam all depend on it.", [
        ("The Messages API: Stateless by Design", 24, ""),
        ("Prompt Engineering That Survives Production", 30, ""),
        ("Structured Output and Validation Loops", 28, ""),
        ("Thinking, Effort and Model Selection", 20, ""),
        ("Prompt Caching and Batches: The Economics", 26, ""),
        ("Context Engineering", 28, ""),
     ]),
    ("II", "AI04", "Vector Databases and Search", "Support",
     "Moved before RAG, because RAG is built on it. Deepens HLD M21A rather than repeating it.", [
        ("Choosing and Operating Embedding Models", 22, ""),
        ("ANN Indexes in Production: Filtering and Quantization", 28, ""),
        ("Hybrid Search and Reranking", 24, ""),
        ("Choosing a Vector Store: pgvector to Dedicated Engines", 22, ""),
        ("Design a Multi-Tenant Vector Search Platform", 40, "DP"),
     ]),
    ("II", "AI05", "RAG", "Support",
     "Your third topic. Provenance (TS 5.6) is the part the exam tests.", [
        ("Why RAG Exists and When Not to Use It", 22, ""),
        ("Ingestion and Chunking", 28, ""),
        ("Contextual Retrieval", 20, "PILOT"),
        ("Query Understanding, Context Assembly and Citations", 24, ""),
        ("The RAG Family: Graph, SQL, Agentic, Multimodal, Self-RAG", 26, ""),
        ("Evaluating RAG", 24, ""),
        ("Design an Enterprise Knowledge Assistant", 42, "DP"),
     ]),
    ("III", "AI06", "AI Agents and Agentic AI", "Core",
     "Your fourth topic. The exam's largest area: Domains 1, 2 and part of 5.", [
        ("Workflows or Agents: Building Effective Agents", 26, ""),
        ("The Agentic Loop", 26, ""),
        ("Designing Tools an Agent Can Use", 28, ""),
        ("MCP: Architecture and Primitives", 30, ""),
        ("MCP: Integration, Servers and Errors", 24, ""),
        ("Multi-Agent Orchestration: Anthropic's Research System", 32, ""),
        ("Sessions, Memory and Long-Running Harnesses", 28, ""),
        ("Hooks, Enforcement, Escalation and Humans in the Loop", 26, ""),
        ("Design a Customer Support Resolution Agent", 42, "DP"),
        ("Design a Multi-Agent Research System", 42, "DP"),
     ]),
    ("III", "AI07", "AI Frameworks", "Low",
     "Your fifth topic. When a framework helps, and when the SDK is better.", [
        ("Do You Need a Framework? SDK to Managed Agents", 24, ""),
        ("LangChain and LangGraph", 26, ""),
        ("LlamaIndex and Other Frameworks", 20, ""),
        ("Spring AI and LangChain4j", 28, ""),
     ]),
    ("IV", "AI08", "AI Application Development in Java", "Low",
     "Your seventh topic. Compiled Java throughout, with the Banking Assistant as the capstone.", [
        ("Claude in Spring Boot: Client, Config, Errors", 28, ""),
        ("Streaming to Users: SSE and Virtual Threads", 24, ""),
        ("A Production Agent Loop and Tools in Java", 30, ""),
        ("Building an MCP Server in Java", 26, ""),
        ("A RAG Service With pgvector", 30, ""),
        ("Async AI: Kafka, Batches and Workflows", 26, ""),
        ("Testing AI Code", 24, ""),
        ("Design a Banking Transaction Analysis Assistant", 40, "DP"),
     ]),
    ("IV", "AI09", "AI Security", "Indirect",
     "First half of your eighth topic. Exam tests least privilege and hooks, not a security domain.", [
        ("Threat Modelling an AI System", 24, ""),
        ("Prompt Injection: The Problem That Has No Patch", 30, ""),
        ("Securing Tools, MCP and Sandboxes", 26, ""),
        ("PII, Tenancy, RBAC, Secrets and Audit", 26, ""),
        ("Jailbreaks, Guardrails and Responsible AI", 26, ""),
     ]),
    ("IV", "AI10", "AI Production Engineering", "Partial",
     "Second half of your eighth topic. Real production architectures live here.", [
        ("Reliability: Retries, Rate Limits and Fallbacks", 26, ""),
        ("AI Gateways and Model Routing", 24, ""),
        ("Evals: Datasets, Hallucination and CI Gates", 30, ""),
        ("Observability: Traces, Tokens and Cost", 24, ""),
        ("Cost Engineering and Semantic Caching", 24, ""),
        ("Deployment, Multi-Tenancy and Disaster Recovery", 24, ""),
        ("Design an LLM Gateway", 42, "DP"),
     ]),
    ("IV", "AI11", "Advanced AI Topics", "Low",
     "Your ninth topic. Beyond the exam, for senior interviews and judgment.", [
        ("Fine-Tuning, LoRA and When Not to Bother", 24, ""),
        ("Self-Hosting Open Models and Quantization", 24, ""),
        ("Interpretability and Alignment Research", 24, ""),
        ("Autonomy: Code Execution and Computer Use", 24, ""),
        ("Event-Driven AI, Workflow Engines and Sagas", 26, ""),
     ]),
    ("V", "AI12", "Claude Code", "Core",
     "Added: Domain 3 is 20% of the exam and needs a teaching home before revision.", [
        ("How Claude Code Works and Its Built-In Tools", 24, ""),
        ("CLAUDE.md, Rules and Memory", 24, ""),
        ("Commands, Skills, Subagents and Hooks", 28, ""),
        ("Plan Mode and Iterative Refinement", 22, ""),
        ("Claude Code in CI/CD", 24, ""),
     ]),
    ("V", "AI13", "Certification Preparation", "Exam",
     "The heaviest focus, as you asked. Reviews and tests; it doesn't reteach.", [
        ("The Exam Decoded: How Scenario Questions Are Built", 24, "NEW-CERT"),
        ("Domain 1 Review and Question Bank", 58, "NEW-CERT"),
        ("Domain 2 Review and Question Bank", 42, "NEW-CERT"),
        ("Domain 3 Review and Question Bank", 46, "NEW-CERT"),
        ("Domain 4 Review and Question Bank", 46, "NEW-CERT"),
        ("Domain 5 Review and Question Bank", 36, "NEW-CERT"),
        ("The Six Scenarios Walked Through", 36, "NEW-CERT"),
        ("Question Types Lab: All 18 Formats", 36, "NEW-CERT"),
        ("Mock Exam 1", 42, "NEW-CERT"),
        ("Mock Exam 2", 42, "NEW-CERT"),
        ("Mock Exam 3", 42, "NEW-CERT"),
        ("Final Revision Handbook and Readiness Check", 24, "NEW-CERT"),
     ]),
]

PARTS = {"I": "Foundations", "II": "Retrieval", "III": "Agents and Frameworks",
         "IV": "Building for Production", "V": "Certification"}


def main():
    lessons, lines = [], []
    grand_pages = grand_lessons = dps = 0
    summary = ["| Part | Module | Title | Lessons | Design problems | Pages | Exam value |",
               "| --- | --- | --- | ---: | ---: | ---: | --- |"]
    detail = []
    pages_by_value = {}
    for part, code, title, value, note, items in MODULES:
        pages = sum(p for _, p, _ in items)
        module_dps = sum(1 for _, _, t in items if t == "DP")
        grand_pages += pages
        grand_lessons += len(items)
        dps += module_dps
        pages_by_value[value] = pages_by_value.get(value, 0) + pages
        summary.append(f"| {part} {PARTS[part]} | **{code}** | {title} | {len(items)} | "
                       f"{module_dps} | {pages} | {value} |")
        detail.append(f"\n## {code}: {title} ({len(items)} lessons, ~{pages} pages)\n")
        detail.append(f"{note}\n")
        detail.append("| # | Lesson | Pages | Type |\n| ---: | --- | ---: | --- |")
        for i, (name, budget, tag) in enumerate(items, 1):
            label = {"DP": "Design problem (10 parts, 6 diagrams)", "PILOT": "Drafted as pilot",
                     "NEW-CERT": "Certification", "": "Lesson"}[tag]
            detail.append(f"| {i} | {name} | {budget} | {label} |")
            lessons.append({"module": code, "n": i, "title": name, "pages": budget,
                            "tag": tag, "words": budget * WORDS_PER_PAGE, "status": "planned"})
    summary.append(f"| | | **Total** | **{grand_lessons}** | **{dps}** | **{grand_pages}** | |")

    core = sum(v for k, v in pages_by_value.items() if k in ("Core", "Exam"))
    words = grand_pages * WORDS_PER_PAGE
    facts = {"lessons": grand_lessons, "design_problems": dps, "pages": grand_pages,
             "words": words, "exam_core_pages": core}
    (ROOT / "00-planning" / "lessons.json").write_text(
        json.dumps({"facts": facts, "lessons": lessons}, indent=1), encoding="utf-8")

    intro = (ROOT / "00-planning" / "ROADMAP_INTRO.md").read_text(encoding="utf-8")
    body = intro.replace("{{SUMMARY}}", "\n".join(summary)).replace(
        "{{LESSONS}}", str(grand_lessons)).replace("{{PAGES}}", f"{grand_pages:,}").replace(
        "{{DPS}}", str(dps)).replace("{{WORDS}}", f"{words:,}").replace(
        "{{CORE}}", str(core))
    out = body + "\n# Lesson-by-lesson plan\n" + "\n".join(detail) + "\n"
    (ROOT / "00-planning" / "COURSE_ROADMAP.md").write_text(out, encoding="utf-8")
    print(facts, pages_by_value)


if __name__ == "__main__":
    main()
