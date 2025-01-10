#include <time.h>

// --- VARIÁVEIS --- //

// Pino de leitura do sensor
const int pin = 34;

// Horários de operação (em segundos desde meia-noite) e 
const int irrigationTimes[3] = {8 * 3600, 14 * 3600, 19 * 3600}; // 08:00, 14:00, 19:00
// const int irrigationTimes[3] = {12 * 3600 + 23 * 60, 12 * 3600 + 30 * 60, 12 * 3600 + 37 * 60}; // Testes

// Margem de tempo, 5 minutos antes do funcionamento da bomba
const int offset = 5 * 60;
// const int offset = 1 * 60;

// Variável de tempo atual
tm timeinfo;


// --- PROGRAMA PRINCIPAL --- //

void setup() {

  Serial.begin(115200);

  // Conecta ao WiFi para sincronizar o relógio
  connectWiFi();
  configTime(-3 * 3600, 0, "pool.ntp.org"); // Configura NTP com fuso horário de Brasília (-3 horas)
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Erro ao obter horário");
    deepSleepUntilMidnight();
  }

  // Descobrindo o horário atual
  int currentTime = timeinfo.tm_hour * 3600 + timeinfo.tm_min * 60 + timeinfo.tm_sec;

  // Com base no horário atual, existem três opções:

  // 1. Já foram todas as irrigações
  if (currentTime > irrigationTimes[2]){
    Serial.println("Está tarde, dormindo até meia noite...");
    deepSleepUntilMidnight();
  }

  // Se chegou até aqui, currentTime está antes do tempo mais tarde. Ou seja, está esperando algum horário chegar

  // Precisamos descobrir qual é a hora que estamos aguardando
  int nextTime = -1;
  for (int i = 0; i < 3; i++){
    if (currentTime < irrigationTimes[i]){
      nextTime = irrigationTimes[i];
      break;
    }
  }

  // A hora que estamos aguardando é nextTime.
  // Precisamos saber se falta muito ou se já está na hora

  // 2. Ainda não está na hora, precisa dormir
  if (currentTime < nextTime - offset){
    int sleepTime = nextTime - offset - currentTime;
    Serial.printf("Está cedo ainda, dormindo por %d segundos...\n", sleepTime);
    deepSleep(sleepTime);
  }

  // 3. Está na hora certa, vamos coletar os dados
  Serial.println("Chegou a hora! Vou coletar os dados do sensor.");
  char result = collectData(nextTime + offset);

  // Agora precisamos enviar o dado coletado ao BD
  if (result == 's'){
    Serial.println("Sucesso! Enviando dados ao servidor...");
  }
  else if (result == 'f'){
    Serial.println("Falhou! Enviando dados ao servidor...");
  }
  else {
    Serial.println("Falha no sensor! Enviando dados ao servidor...");
  }

  insertData(nextTime, result);
  
  // Firebase.begin(firebase_host, firebase_auth);
  Serial.println("Acabou, vou voltar a dormir.");

  // Por fim, precisamos dormir até a próxima hora ou até a meia-noite.

  // Pegando o horário atual
  if (getLocalTime(&timeinfo)) {
    currentTime = timeinfo.tm_hour * 3600 + timeinfo.tm_min * 60 + timeinfo.tm_sec;
  }
  else{
    deepSleepUntilMidnight();
  } 

  // Calculando o próximo horário
  nextTime = -1;
  for (int i = 0; i < 3; i++){
    if (currentTime < irrigationTimes[i]){
      nextTime = irrigationTimes[i];
      break;
    }
  }

  // Dormir até a hora determinada
  if(nextTime == -1){
    deepSleepUntilMidnight();
  }
  else{
    int sleepTime = nextTime - offset - currentTime;
    deepSleep(sleepTime);
  }

}



void loop(){
    // Não utilizado porque estamos usando deep sleep
}