#include <WiFi.h>
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

  Serial.printf("Dormindo até meia-noite, %d segundos...\n", sleepTime);
  esp_sleep_enable_timer_wakeup(sleepTime * 1000000LL);
  esp_deep_sleep_start();
  return;
}