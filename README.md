# mm-recommendation-service

Provides personalized movie recommendations based on user interactions.  

## 🚀 Features

- Generates recommendations based on user activity and graph similarity.
- Updates Neo4j recommendations graph in real time through Dapr pub/sub (Kafka backend recommended).
- Guarantees a fixed number of recommendations (typically five), supplementing with popular movies as needed.
- Packaged as a container image using Jib, ready for deployment to Kubernetes or Docker environments.
- Fully environment-variable-driven configuration.


## 🧠 Recommendation algorithm

This service uses **Neo4j Graph Data Science (GDS)** library to compute movie recommendations.

### 📊 Graph model

- **Nodes:**  
  - `User`  
  - `Movie`  

- **Relationships:**  
  - `(User)-[:RATED {rating}]->(Movie)`  
  - `(User)-[:WATCHED]->(Movie)`  


### 🔍 Algorithm: Node Similarity

We run **[Node Similarity](https://neo4j.com/docs/graph-data-science/current/algorithms/node-similarity/)** from Neo4j GDS to find movies similar to those the user has already interacted with.

1. **Subgraph projection** – select relevant users and movies  
2. **Compute similarity scores** – based on shared watchers and ratings  
3. **Rank movies** – by similarity to the user’s watched/rated movies  
4. **Return top N movies** – fill missing slots with trending titles  


By default, it uses **Jaccard Similarity**:

**Formula:**
similarity(A, B) = |neighbors(A) ∩ neighbors(B)| / |neighbors(A) ∪ neighbors(B)|

**Interpretation:**
- `1.0` → nodes share exactly the same neighbors
- `0.0` → nodes have no neighbors in common

#### Step 1: Neighbor sets

| Movie   | Neighbors (Users) |
|--------|------------------|
| MovieA | {User1} |
| MovieB | {User1, User2} |
| MovieC | {User3} |

#### Step 2: Compute similarities

| Pair (Movie1, Movie2) | Intersection | Union | Jaccard Similarity |
|----------------------|-------------|------|-------------------|
| (MovieA, MovieB) | {User1} → 1 | {User1, User2} → 2 | **1 ÷ 2 = 0.5** |
| (MovieB, MovieC) | {} → 0 | {User1, User2, User3} → 3 | **0 ÷ 3 = 0.0** |

> ⚡ **Why Graph Node Similarity?**  
> It is lightweight, scalable, and supports streaming results directly from the GDS library — ideal for near-real-time recommendations.





## 🛠️ Tech Stack


| Technology              | Version        |
|-------------------------|---------------|
| Java (JDK)              | 17            |
| Kotlin                  | 2.1.10        |
| Ktor (server/client)    | 3.0.3         |
| Neo4j Java Driver       | 5.20.0        |


## 🛠️ Building and Running with Jib

[Jib](https://github.com/GoogleContainerTools/jib) provides container image builds for Java/Kotlin applications without the need for a Dockerfile.

**To build a local container image:**

```bash
LOCAL_BUILD=true mvn compile jib:dockerBuild \
  -Dimage=mm-recommendation-service:local
```

## ⚙️ Environment Variables

Environment variables are defined in `.env.example`.

- Copy `.env.example` to `.env`
- Fill in the required values for your local or production setup
  
## 📄 API Documentation

- **OpenAPI docs:** `${context.path}/docs`
- **Swagger UI:** `${context.path}/docs/swagger

_Replace `${context.path}` with deployment context._



