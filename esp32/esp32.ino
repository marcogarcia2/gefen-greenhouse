#include <time.h>
// #include <IOXhop_FirebaseESP32.h>
// #include <ArduinoJson.h>

// --- VARIÁVEIS GLOBAIS --- //

// Margem de tempo em segundos antes do funcionamento da bomba (5 minutos)
const unsigned int offset = 300;

// Variável de tempo atual
tm timeinfo;

// --- PROGRAMA PRINCIPAL --- //

void setup() {

  Serial.begin(115200);

  // Conecta ao WiFi para sincronizar o relógio
  while (!connectWiFi());

  configTime(-3 * 3600, 0, "pool.ntp.org"); // Configura NTP com fuso horário de Brasília (-3 horas)

  if (!getLocalTime(&timeinfo)) {
    Serial.println("Erro ao obter horário. Reiniciando o sistema...");
    deepSleep(5); // Dorme por 10 segundos para tentar novamente
  }

  blueLED(true);

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
  if (currentTime < nextTime - (2*offset)){
    int sleepTime = nextTime - (2*offset) - currentTime;
    Serial.printf("Está cedo ainda, dormindo até %s, por %d segundos...\n", getTimeString(nextTime), sleepTime);
    deepSleep(sleepTime);
  }

  // 3. Está na hora certa, vamos coletar os dados
  
  // Se estiver cedo demais, vamos dar um delay para ligar somente 120 segundos antes
  int t = nextTime - 120;
  if (currentTime < t){
    int delayTime = t - currentTime;
    Serial.printf("Agurdando %d segundos até a hora da coleta!\n", delayTime);
    delay(1000 * delayTime);
    currentTime += (delayTime);
  }

  Serial.println("Chegou a hora! Coletando dados do sensor.");
  float volume = 0.0;
  char result = collectData(currentTime, nextTime + offset, &volume);

  // Agora precisamos enviar o dado coletado ao BD
  if (result == 'S') Serial.println("Sucesso! Enviando dados ao servidor...");
  else Serial.println("Falhou! Enviando dados ao servidor...");
  
  // Garante que a conexão WiFi existe
  reconnectWiFi();

  // Insere os dados no BD 
  insertData(nextTime, result, volume);
  Serial.println("Acabou, vou voltar a dormir.");

  // Por fim, precisamos dormir até a próxima hora ou até a meia-noite.

  // Pegando o horário atual
  if (getLocalTime(&timeinfo)) {
    currentTime = timeinfo.tm_hour * 3600 + timeinfo.tm_min * 60 + timeinfo.tm_sec;
  }
  else{
    Serial.println("Erro ao obter horário. Tentando novamente...");
    deepSleep(5);
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
    yield();
}