# AI04: References and Verification

Numbers are recomputed by `scripts/verify_ai04.py`, or come from running `code/ai04-vector/`. Last checked 2026-11-04.

## Code status

| Lesson | File | Status |
| --- | --- | --- |
| L1 | `EmbeddingOps.java` | Run, and output quoted exactly. Synthetic vectors with decaying variance, not a real model. The "other model" is a dimension shuffle |
| L1 | `pgvector/.../EmbeddingClient.java` | **Compiled, not run**. No Voyage key |
| L2 | `VectorLab.java` | Run, and output quoted exactly. Synthetic clusters: 20,000 vectors, 128 dimensions, 40 clusters, 200 queries |
| L3 | `HybridLab.java` | Run, and output quoted exactly. Invented corpus of 12 and 10 labeled queries. The "embedder" is a synonym table |
| L4 | `pgvector/.../ChunkRepository.java` | **Compiled, not run**. No Postgres available |
| L4 | SQL in the lesson | **Not run**. Follows pgvector's docs |
| L5 | `TenantPlan.java` | Run, and output quoted exactly. All inputs are stated assumptions |

## Findings worth remembering, with their limits

| Finding | Limit |
| --- | --- |
| 10% independent filter left 3.9 of 40 candidates | Agrees with pgvector's documented "about 4", which supports the simulation |
| 10% correlated filter gave 88% empty results | One synthetic shape. The size of the effect depends on how clustered tenants are |
| int8 recall 0.938, binary 0.125 | **Specific to tightly clustered synthetic data.** Binary's poor result may not hold on real embeddings. The lesson says so |
| All fusion methods scored 1.00 | The toy test can't separate them. The lesson says so, and doesn't claim that RRF won |
| IVF with 1 probe at 0.491 | Clean clusters. Real data will differ |

## Hand-derived figures

All match `verify_ai04.py`: L1 Q2, L2's `(1−p)ⁿ` values, L3's MRR values, 12.3 GB in L4, L5's ratios, 28.3 GB for the largest tenant, and Exercises 1, 3 and 5.

**Errors found in my own drafts:** L5 used a fan-out to 7 shards while designing for 3, and now uses 3 throughout. The 400-searches-per-replica figure in L5 is a **placeholder**, and it's labeled that way.

## Sources read

| Claim | Source | How it was checked |
| --- | --- | --- |
| No Anthropic embedding model, Voyage models, `input_type`, Matryoshka, quantization | [Claude embeddings guide](https://platform.claude.com/docs/en/build-with-claude/embeddings) | Read in full |
| Index types, limits, defaults, filtering, iterative scans, binary quantization, recall advice | [pgvector README](https://github.com/pgvector/pgvector) | Read through a fetch tool, showing v0.8.7 |
| Multi-tenancy, `is_tenant`, tiers, 1,000 collections | [Qdrant multitenancy](https://qdrant.tech/documentation/guides/multiple-partitions/) | Read through a fetch tool |
| Namespaces, deletion, metadata filter downsides | [Pinecone multitenancy](https://docs.pinecone.io/guides/index-data/implement-multitenancy) | Read through a fetch tool |
| Normalization and score ranker processors | [OpenSearch hybrid search](https://docs.opensearch.org/latest/vector-search/ai-search/hybrid-search/index/) | Read through a fetch tool |
| Cross-encoder description | [Voyage rerankers](https://www.mongodb.com/docs/voyageai/models/rerankers/) | Read. The API request shape wasn't visible, so no rerank client is included |
| 150 to 20, 2.9% to 1.9% | [Contextual Retrieval](https://www.anthropic.com/engineering/contextual-retrieval) | Read, in the pilot |
| Filtering, RRF, sharding, migration | HLD M21A | Read. Cross-referenced, not re-taught |

## Stated from general knowledge

Milvus, Weaviate, Elasticsearch, FAISS and hnswlib descriptions. BM25's formula and default values. NDCG. HikariCP's connection reuse behavior. Row-level security with `current_setting`. Check product details before making a decision.

## Not verified

- Whether pgvector's HNSW index and row-level security behave as the lesson's SQL expects on partitioned tables under your version. **Test this.**
- `pgvector-java` 0.1.6 and the Postgres driver version are the latest listed on Maven Central on the day I checked.

## Engineering recommendations, not facts

Every production architecture and the L5 design, filter strategy thresholds, the decision method for choosing a store, the int8 choice in L5, the 50–100 starter query count, and the leaning towards a dedicated engine at 200 million chunks. **None describes a specific company's system.**

## Depth note

HLD M21A already teaches this module's foundations, and it isn't repeated. Lessons came in at 86 of 136 budgeted pages for that reason.
