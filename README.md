# mm-recommendation-service

Provides personalized movie recommendations based on user interactions.  

## 🚀 Features

- Generates recommendations based on user activity and graph similarity.
- Updates Neo4j recommendations graph in real time through Dapr pub/sub (Kafka backend recommended).
- Guarantees a fixed number of recommendations (typically five), supplementing with popular movies as needed.
- Packaged as a container image using Jib, ready for deployment to Kubernetes or Docker environments.
- Fully environment-variable-driven configuration.

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
