# Use a imagem oficial do OpenJDK como base
FROM eclipse-temurin:17-jdk-jammy

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia os arquivos de build e o pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Baixa as dependências do projeto para criar uma camada de cache no Docker
RUN ./mvnw dependency:go-offline

# Copia o código-fonte da aplicação
COPY src ./src

# Compila a aplicação e empacota em um JAR executável
# Pula os testes, pois já devem ter sido executados no pipeline de CI
RUN ./mvnw clean package -Dmaven.test.skip=true

# Expõe a porta em que a aplicação Spring Boot será executada
EXPOSE 8080

# Define o comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "target/taskandpay-0.0.1-SNAPSHOT.jar"]
