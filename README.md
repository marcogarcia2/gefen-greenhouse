# IoT-Greenhouse

Este repositório contém a minha implementação de um sistema de monitoramento de irrigação de uma estufa através de um aplicativo de celular, utilizando ESP32. 

Este projeto foi um pedido de um amigo, a fim de automatizar o processo de rega de plantar para facilitar os experimentos.

## GEFEN - CENA/USP
Meu amigo Gustavo é estudante de Engenharia Agronômica pela ESALQ (Escola Superior de Agricultura Luiz de Queiroz), em Piracicaba, São Paulo. Além de ser estudante de graduação, ele é membro e pesquisador do GEFEN (Grupo de Estudos em Fertilizantes Especiais e Nutrição) do CENA (Centro de Energia Nuclear na Agricultura). Ambos os Institutos pertencem à USP, a gloriosa Universidade de São Paulo. 

imagem da logo do CENA e da Esalq.

O CENA possui uma estufa, fundada em 1962 e que foi tombada como patrimônio histórico da USP. Essa estufa é utilizada pelo GEFEN, para a realização de diversos experimentos em diferentes plantas.

imagem da estufa e logo do gefen


## Ilustrando o problema
A fim de automatizar o processo de irrigação, o GEFEN comprou uma bomba de água e desenvolveu um sistema de irrigação automático com um timer. Este timer foi configurado para ligar a bomba três vezes ao dia, impulsionando a água através de um tubo gotejador, assim regando as plantas automaticamente. 

imagem da bomba e do tubo gotejador

Foi aí que meu amigo Gus entrou em contato comigo. Ele relatou que por vezes esse sistema falha, ou porque o timer falhou inesperadamente ou porque entrou ar no sistema da bomba e nenhuma água foi bombeada, assim compromentendo o experimento e podendo levar a morte das plantas. 

Ele me pediu para desenvolver uma solução, que através de um aplicativo de celular, avisasse se a bomba funcionou ou não. Com isso, caso o sistema da bomba falhasse, ele seria avisado e o experimento seria salvo. Então essa foi a hora de colocar meus conhecimentos em prática... ou não. 

## Visão geral da solução
É importante ressaltar que eu nunca havia feito um projeto grande como esse antes. Muito do que eu utilizei no projeto eu não aprendi explicitamente na faculdade, acabei descobrindo através de pesquisa de projetos semelhantes.

O sistema embarcado constitui-se de uma ```ESP32``` conectada a um sensor de fluxo de água YF-S201. Três vezes ao dia, durante o horário suposto de funcionamento da bomba, a ESP32 liga e começa a escutar dados do sensor. Todos os códigos da ESP32 foram desenvolvidos em linguagem C++.

O aplicativo foi desenvolvido no ```Android Studio```, na linguagem ```Kotlin```. O app constitui-se de apenas três telas simples, sendo elas:
- Uma home, para visualizar as irrigações do dia de hoje;
- Um histórico, para visualizar as irrigações dos dias anteriores;
- Uma página de configurações, para alterar os horários de irrigação.

A integração do app com o sistema embarcado foi feito através do ```Firebase``` do Google. Ele é uma solução gratuita de servidor e de banco de dados em tempo real desenvolvido pelo Google.

Portanto, pode-se dizer que o projeto consiste em três grandes partes: o sistema embarcado, o aplicativo e o servidor que interliga os dois.

imagem ilustração do sistema

# O Sistema Embarcado


# O Aplicativo


# O Servidor





