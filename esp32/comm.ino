#include <WiFi.h>
#include <IOXhop_FirebaseESP32.h>
#include <ArduinoJson.h>
#include <string.h>

#include "secret.h"

#define MAX_ATTEMPTS 3

// Função que conecta-se ao wifi
void connectWiFi() {

  // Três tentativas no total, caso falhe, rebootar e tentar de novo
  for (int attempts = 0; attempts < MAX_ATTEMPTS; attempts++){

    // Conexão em WiFi tradicional
    WiFi.begin(ssid, password);

    // Conexão no eduroam
    // WiFi.begin(ssid, WPA2_AUTH_PEAP, EAP_IDENTITY, EAP_USERNAME, EAP_PASSWORD); // Para redes como eduroam
    
    Serial.print("Conectando ao WiFi");
    
    int i = 0; // Contador de tentativas
    while (WiFi.status() != WL_CONNECTED && i < 30) { // 100 segundos de tentativa, aproximadamente
      delay(1000);
      Serial.print(".");
      i++;
    }
    
    // Se a conexão for realizada com sucesso:
    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("\nWiFi conectado!");
      Serial.print("IP: ");
      Serial.println(WiFi.localIP());
      break;
    } 

    // Se não, dá mais chances ou reseta o sistema
    // else {

    WiFi.disconnect();
    
    switch(attempts){
      case 0: 
        Serial.println("\nERRO: A primeira tentativa de conexão falhou. Tentando novamente...");
        break;

      case 1:
        Serial.println("\nERRO: A segunda tentativa de conexão também falhou. Tentando novamente...");
        break;
      
      case 2:
        Serial.println("\nERRO: A última tentativa de conexaão também falhou. Tentando novamente em 5 minutos...");
        deepSleep(5*60);
        break;

      default:
        break;
    }
    
  }

}


// Função que descobre a ação a ser tomada com base no horário atual
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
bool insertData(int nextTime, char result, float volume){

  char path[60] = "/Estufa/";
  char aux_path[60];
  char dateBuffer[15]; 
  char timeBuffer[10];

  // Pegando o dia de hoje e o horário
  strcpy(dateBuffer, getDate());
  strcpy(timeBuffer, getTimeString(nextTime));

  // Sempre reescrevendo o número de vasos para ter redundância
  strcat(path, dateBuffer);
  strcpy(aux_path, path);
  strcat(aux_path, "/vasos");
  Firebase.setInt(aux_path, Firebase.getInt("Estufa/000v"));

  // Continuando a escrever o path
  strcat(path, "/");
  strcat(path, timeBuffer);

  //  O path vai ficar fixo, preciso inserir no status e caso S, volume
  strcpy(aux_path, path);
  strcat(aux_path, "/status");

  // Antes de inserir, verifica se ele já não existe por segurança
  if (Firebase.getString(aux_path) == "S"){
    Serial.println("Dado já existente. A inserção falhou.");
    return false;
  }

  // Transforma o result em String
  char newResult[2] = {result, '\0'};

  // Inserindo o dado de status
  Firebase.setString(aux_path, newResult);
  Serial.printf("Status inserido com sucesso: %c\n", result);

  // Caso seja sucesso, vamos guardar a informação de volume também
  if (result == 'S'){
    
    // Criando o path correto
    strcpy(aux_path, path);
    strcat(aux_path, "/volume");

    // Inserindo o dado de volume
    Firebase.setFloat(aux_path, volume);
    Serial.printf("Volume inserido com sucesso: %f\n", volume);
  }

  if(!Firebase.failed()){
    Serial.println("Todos os dados inseridos com sucesso.");
    return true;
  }
  else{
    Serial.println("Falha na inserção de dados.");
    return false;
  }

}
