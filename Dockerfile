FROM golang:1.22-alpine
RUN apk upgrade --no-cache && apk add --no-cache curl
WORKDIR /app
COPY . .
RUN if [ ! -f go.mod ]; then go mod init myapp; fi && \
    go mod tidy && \
    go build -o main main.go
EXPOSE 80
CMD ["./main"]