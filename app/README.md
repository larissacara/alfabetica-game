# Alfabetica Game

Um jogo de palavras em Android desenvolvido com Jetpack Compose, onde os jogadores precisam pensar em palavras de uma categoria específica usando as letras disponíveis.

## 🎮 Características

- **Layout Adaptativo**: O jogo se adapta automaticamente ao número de letras selecionadas, distribuindo-as em 3 linhas de forma inteligente
- **Timer Centralizado**: Timer sempre no centro da linha do meio, com número par de letras ao redor
- **Categorias Personalizáveis**: Adicione, edite e remova categorias de palavras
- **Configurações Flexíveis**: Escolha quais letras aparecem no jogo, duração do timer, som e vibração
- **Interface Moderna**: Design responsivo com animações e gradientes
- **Modo Paisagem**: Otimizado para dispositivos móveis em orientação horizontal

## 🚀 Tecnologias Utilizadas

- **Android**: Kotlin + Jetpack Compose
- **UI**: Material Design 3
- **Arquitetura**: MVVM com State Management
- **Animações**: Compose Animation APIs

## 📱 Como Jogar

1. **Começar**: Toque em "Começar" para iniciar uma partida
2. **Categorias**: Configure suas categorias personalizadas (Filme, Animal, Fruta, etc.)
3. **Ajustes**: Selecione as letras que aparecerão no jogo e configure o timer
4. **Jogar**: Use as letras disponíveis para formar palavras da categoria escolhida
5. **Timer**: O timer no centro controla o tempo de cada rodada

## ⚙️ Configurações

### Letras do Jogo
- Selecione quais letras do alfabeto aparecerão no jogo
- Mínimo de 5 letras recomendado para uma boa experiência
- O layout se adapta automaticamente ao número selecionado

### Timer
- Configure a duração de cada rodada (5s, 10s, 15s, 20s, 30s, 60s)
- Timer sempre centralizado na linha do meio

### Categorias
- Adicione suas próprias categorias
- Edite ou remova categorias existentes
- Categorias padrão: Nome, Animal, Fruta, País, Cor, Objeto, Profissão

## 🎨 Layout Adaptativo

O jogo implementa um sistema inteligente de distribuição de letras:

- **Poucas letras (≤6)**: Distribuição equilibrada entre as 3 linhas
- **Muitas letras**: Prioriza a linha do meio com número par de letras para centralizar o timer
- **Linha do meio**: Sempre com número par de letras para manter o timer no centro
- **Responsivo**: Adapta-se a diferentes tamanhos de tela

## 🛠️ Instalação

1. Clone o repositório:
```bash
git clone https://github.com/larissacara/alfabetica-game.git
```

2. Abra o projeto no Android Studio

3. Sincronize as dependências do Gradle

4. Execute no dispositivo ou emulador Android

## 📋 Requisitos

- Android 7.0 (API 24) ou superior
- Orientação paisagem recomendada
- Tela de pelo menos 5 polegadas para melhor experiência

## 🤝 Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para:

- Reportar bugs
- Sugerir novas funcionalidades
- Enviar pull requests
- Melhorar a documentação

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👨‍💻 Desenvolvedor

Desenvolvido com ❤️ usando Jetpack Compose e Material Design 3.

---

**Divirta-se jogando Alfabetica!** 🎉