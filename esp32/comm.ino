#include <WiFi.h>
#include <IOXhop_FirebaseESP32.h>
#include <ArduinoJson.h>
#include <string.h>

#include "secret.h"

// Função que conecta-se ao wifi
void connectWiFi() {
  WiFi.begin(ssid, password);
  Serial.print("Conectando ao WiFi...");
  
  int attempts = 0; // Contador de tentativas
  while (WiFi.status() != WL_CONNECTED && attempts < 20) { // 20 tentativas (~10 segundos)
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi conectado!");
    Serial.print("IP: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\nErro ao conectar ao WiFi!");
  }
}

// Função que trata os dados e os insere no BD
bool insertData(int nextTime, char result){

  // O dado que desejamos inserir é do tipo ("2024-01-09", "08:00", 's')
  char* date = getDate();
  char* time = getTime(nextTime);

  // Serial.printf("Dados a serem inseridos: (%s, %s, %c)\n", date, time, result);
  
  char path[100] = "/Estufa/";
  strcat(path, date);
  strcat(path, "/");
  strcat(path, time);
  char newResult[2] = {result, '\0'};

  // Inicializa a conexão com o banco de dados
  Firebase.begin(firebase_host, firebase_auth);
  Firebase.setString(path, newResult);

  if(!Firebase.failed()){
    Serial.println("Dados inseridos com sucesso.");
    return true;
  }
  else{
    Serial.println("Falha na inserção de dados.");
    return false;
  }

}
