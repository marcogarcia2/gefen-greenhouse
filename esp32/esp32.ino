#include <time.h>
#include <IOXhop_FirebaseESP32.h>
#include <ArduinoJson.h>

// --- VARIÁVEIS --- //

// Pino de leitura do sensor
const int pin = 34;

// Margem de tempo, 5 minutos antes do funcionamento da bomba
// const int offset = 5 * 60;
const int offset = 5 * 60;

// Variável de tempo atual
tm timeinfo;


// --- PROGRAMA PRINCIPAL --- //

void setup() {

  Serial.begin(115200);

  // Conecta ao WiFi para sincronizar o relógio
  connectWiFi();
  configTime(-3 * 3600, 0, "pool.ntp.org"); // Configura NTP com fuso horário de Brasília (-3 horas)
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Erro ao obter horário. Tentando novamente em 15 minutos...");
    deepSleep(15*60); // Dorme por 15 minutos para tentar novamente
  }

  // Descobrindo o horário atual
  int currentTime = timeinfo.tm_hour * 3600 + timeinfo.tm_min * 60 + timeinfo.tm_sec;

  // Lendo os dados do BD, para descobrir qual é o próximo horário
  int nextTime = whatToDo(currentTime);

  // Com base no horário descoberto, existem três opções:

  // 1. Caso já tenha passado todos os horários de irrigação de hoje, dormir até meia noite
  if (!nextTime){
    Serial.println("Está tarde, dormindo até meia noite...");
    deepSleepUntilMidnight();
  }

  // Se chegou até aqui, currentTime é menor que o nextTime. Ou seja, está esperando algum horário chegar

  // A hora que estamos aguardando é nextTime.
  // Precisamos saber se falta muito ou se já está na hora

  // 2. Ainda não está na hora, precisa dormir
  if (currentTime < nextTime - offset){
    int sleepTime = nextTime - offset - currentTime;
    Serial.printf("Está cedo ainda, dormindo até %s, por %d segundos...\n", getTimeString(nextTime), sleepTime);
    deepSleep(sleepTime);
  }

  // 3. Está na hora certa, vamos coletar os dados
  Serial.println("Chegou a hora! Vou coletar os dados do sensor.");
  char result = collectData(nextTime + offset);

  // Agora precisamos enviar o dado coletado ao BD
  if (result == 'S'){
    Serial.println("Sucesso! Enviando dados ao servidor...");
  }
  else if (result == 'F'){
    Serial.println("Falhou! Enviando dados ao servidor...");
  }
  else {
    Serial.println("Falha no sensor! Enviando dados ao servidor...");
  }

  insertData(nextTime, result);
  Serial.println("Acabou, vou voltar a dormir.");

  // Por fim, precisamos dormir até a próxima hora ou até a meia-noite.

  // Pegando o horário atual
  if (getLocalTime(&timeinfo)) {
    currentTime = timeinfo.tm_hour * 3600 + timeinfo.tm_min * 60 + timeinfo.tm_sec;
  }
  else{
    Serial.println("Erro ao obter horário. Tentando novamente em 15 minutos...");
    deepSleep(15*60);
  } 

  nextTime = whatToDo(currentTime);

  // Dormir até a hora determinada
  if(!nextTime){
    Serial.println("Está tarde, dormindo até meia noite...");
    deepSleepUntilMidnight();
  }
  else{
    int sleepTime = nextTime - offset - currentTime;
    Serial.printf("Hoje tem mais, dormindo até %s, por %d segundos...\n", getTimeString(nextTime), sleepTime);
    deepSleep(sleepTime);
  }

}



void loop(){
    // Não utilizado porque estamos usando deep sleep
}