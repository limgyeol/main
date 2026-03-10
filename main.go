package main

import (
   "fmt"
   "log"
   "net/http"
   "os"
   "strings"
)

func main() {
   server := &http.Server{
      Addr:    ":80",
      Handler: nil,
   }

   http.HandleFunc("/healthcheck", healthCheckHandler)
   http.HandleFunc("/v1/static", staticHandler)
   http.HandleFunc("/v1/aip", apiHandler)

   fmt.Println("API server is running on port 8080.")
   log.Fatal(server.ListenAndServe())
}

func healthCheckHandler(w http.ResponseWriter, r *http.Request) {
   w.WriteHeader(http.StatusOK)
   w.Write([]byte("OK"))

   logRequest(r, http.StatusOK)
}

func staticHandler(w http.ResponseWriter, r *http.Request) {
   w.WriteHeader(http.StatusOK)
   w.Write([]byte("static"))

   logRequest(r, http.StatusOK)
}

func apiHandler(w http.ResponseWriter, r *http.Request) {
   name := r.URL.Query().Get("name")

   if strings.Contains(name, "b") {
      http.Error(w, "500 Error", http.StatusInternalServerError)
      logRequest(r, http.StatusInternalServerError)
      return
   } else if strings.Contains(name, "a") && strings.Contains(name, "c") {
      http.Error(w, "400 Error", http.StatusBadRequest)
      logRequest(r, http.StatusBadRequest)
      return
   }

   w.WriteHeader(http.StatusOK)
   w.Write([]byte(fmt.Sprintf("Hello, %s!", name)))

   logRequest(r, http.StatusOK)
}

func logRequest(r *http.Request, statusCode int) {
   logFile, err := os.OpenFile("access.log", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
   if err != nil {
      log.Println("Failed to open log file:", err)
      return
   }
   defer logFile.Close()

   logMessage := fmt.Sprintf(
      "Method: %s | Path: %s | RemoteAddr: %s | StatusCode: %d\n",
      r.Method,
      r.URL.Path,
      r.RemoteAddr,
      statusCode,
   )

   if _, err := logFile.WriteString(logMessage); err != nil {
      log.Println("Failed to write log:", err)
   }
}