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


// Função que descobre a ação a ser tomada
int whatToDo(int currentTime){
  
  // Path para a leitura dos horários de funcionamento
  const String path = "/Estufa/000h/";
  String fullPath;
  
  // Índice para iterar sobre os dados
  int index = 0;
  
  // Conecta-se com o banco de dados
  Firebase.begin(firebase_host, firebase_auth);

  // Loop while, vamos buscar o horário para atuar
  while(true){

    // Lê o dado e atualiza o path para a próxima, converte o tempo para int
    fullPath = path + String(index++);
    String discoveredTime = Firebase.getString(fullPath);
    int newTime = getTimeInt(discoveredTime);

    if (newTime){
      if (newTime > currentTime){
        Serial.printf("Horário descoberto: %s\n", discoveredTime);
        return newTime;
      }
    }
    else break;
    
  }

  return 0;
}


// Função que trata os dados e os insere no BD
bool insertData(int nextTime, char result){

  // O dado que desejamos inserir é do tipo ("2024-01-09", "08:00", 's')
  char* date = getDate();
  char* time = getTimeString(nextTime);

  // Serial.printf("Dados a serem inseridos: (%s, %s, %c)\n", date, time, result);
  
  char path[25] = "/Estufa/";
  strcat(path, date);
  strcat(path, "/");
  strcat(path, time);
  char newResult[2] = {result, '\0'};

  // Inicializa a conexão com o banco de dados
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
