"""AI07 L3: real LlamaIndex with its mock LLM and mock embeddings, so no key is needed.
Mock embeddings carry no meaning, so retrieval order here means nothing. The point is to
see the nodes it builds, and the prompt it sends on your behalf."""
from llama_index.core import Document, Settings, VectorStoreIndex
from llama_index.core.embeddings import MockEmbedding
from llama_index.core.llms import MockLLM
from llama_index.core.node_parser import SentenceSplitter

TEXT = (
    "Card refunds take 5 to 10 business days to appear. Wallet refunds appear within 24 "
    "hours. Bank transfers take up to 14 days because of clearing rules. Orders above $5,000 "
    "require approval from a regional manager. Digital goods can't be refunded once they "
    "have been downloaded. Refunds are issued to the original payment method only."
)


class RecordingLLM(MockLLM):
    last_prompt: str = ""

    def complete(self, prompt, formatted=False, **kwargs):
        self.last_prompt = prompt
        return super().complete(prompt, formatted=formatted, **kwargs)


Settings.embed_model = MockEmbedding(embed_dim=8)
llm = RecordingLLM(max_tokens=8)
Settings.llm = llm

document = Document(text=TEXT, metadata={"source": "refund-policy.md"})
nodes = SentenceSplitter(chunk_size=40, chunk_overlap=8).get_nodes_from_documents([document])
print(f"1. One document became {len(nodes)} nodes")
for node in nodes[:2]:
    print(f"   {node.node_id[:8]} metadata={node.metadata} text={node.text[:58]!r}")
print("   relationships on node 1:", [r.name for r in nodes[1].relationships])

index = VectorStoreIndex(nodes)
engine = index.as_query_engine(similarity_top_k=2)
print("2. Two lines of code ran retrieval, built a prompt and called the LLM")
engine.query("How long do card refunds take?")
print("3. The prompt LlamaIndex sent for you:")
print(llm.last_prompt)
print("4. Default chunk size:", Settings.chunk_size, "overlap:", Settings.chunk_overlap)
