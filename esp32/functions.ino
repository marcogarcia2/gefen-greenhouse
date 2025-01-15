#include <time.h>

// Função que coleta e processa dados do sensor
char collectData(int final) {

  int currentTime = timeinfo.tm_hour * 3600 + timeinfo.tm_min * 60 + timeinfo.tm_sec;

  while (currentTime < final) {

    int value = analogRead(pin);
    Serial.printf("Valor lido pelo sensor: %d\n", value);
    delay(500);

    // Atualiza o tempo atual
    if (getLocalTime(&timeinfo)) {
      currentTime = timeinfo.tm_hour * 3600 + timeinfo.tm_min * 60 + timeinfo.tm_sec;
    } 
    else {
      Serial.println("Erro ao atualizar o tempo!");
      return 'I';
    }
  }

  return 'S';
}

// Função para colocar ESP32 em deep sleep por um tempo determinado
void deepSleep(int sleepTime){

  Serial.printf("Dormindo por %d segundos...\n", sleepTime);
  esp_sleep_enable_timer_wakeup(sleepTime * 1000000LL);
  esp_deep_sleep_start();
  return;
}

// Função para dormir até a meia-noite do próximo dia
void deepSleepUntilMidnight() {
  
  int currentTime = timeinfo.tm_hour * 3600 + timeinfo.tm_min * 60 + timeinfo.tm_sec;
  int sleepTime = 24 * 3600 - currentTime;
  Serial.printf("Dormindo até a meia-note, por %d segundos...\n", sleepTime);

  esp_sleep_enable_timer_wakeup(sleepTime * 1000000LL);
  esp_deep_sleep_start();
  return;
}

// Função que obtém a data de hoje no formato "YYYY-MM-DD"
char* getDate() {
  struct tm timeinfo;

  // Atualiza as informações de tempo local
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Erro ao obter o horário local!");
    return nullptr; // Retorna null em caso de erro
  }

  // Aloca memória para armazenar a data
  static char date[11]; // "YYYY-MM-DD" + null terminator

  // Formata a data como "YYYY-MM-DD"
  snprintf(date, sizeof(date), "%04d-%02d-%02d", 
           timeinfo.tm_year + 1900, // Ano desde 1900
           timeinfo.tm_mon + 1,     // Mês (0 a 11, por isso adicionamos 1)
           timeinfo.tm_mday);       // Dia do mês

  return date; // Retorna o ponteiro para a string formatada
}

// Função que obtém o horário no formato "HH:MM" dado o número de segundos
char* getTimeString(int timeInSeconds) {
  // Calcula horas e minutos
  int hours = timeInSeconds / 3600;
  int minutes = (timeInSeconds % 3600) / 60;

  // Aloca memória para a string do tempo no formato "hh:mm"
  char* newTime = new char[6]; // "hh:mm" + null terminator
  
  // Formata o tempo como "hh:mm"
  snprintf(newTime, 6, "%02d:%02d", hours, minutes);

  return newTime;
}

// Função que obtém o número de segundos dado um horário no formato "HH:MM"
int getTimeInt(String timeInString) {
    // Calcula o número de segundos dado essa string
    int hours = 0;
    int minutes = 0;

    const char* timeInCharP = timeInString.c_str();

    // Usa sscanf para extrair horas e minutos do formato "HH:MM"
    if (sscanf(timeInCharP, "%d:%d", &hours, &minutes) == 2) {
        // Converte horas e minutos para segundos
        return (hours * 3600) + (minutes * 60);
    }

    // Retorna 0 caso o formato seja inválido
    return 0;
}

