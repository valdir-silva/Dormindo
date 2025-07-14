# Dormindo

Aplicativo Android para monitoramento e análise de padrões de sono.

## Sobre o Projeto

Este é um aplicativo Android desenvolvido em Kotlin usando Jetpack Compose e Clean Architecture. O objetivo é fornecer uma solução completa para monitoramento e análise de padrões de sono.

## Tecnologias Utilizadas

- **Kotlin** - Linguagem principal
- **Jetpack Compose** - UI declarativa
- **Material 3** - Design system
- **Room Database** - Persistência local
- **MVVM + Clean Architecture** - Arquitetura do projeto
- **Koin** - Injeção de dependências
- **Kotlin Flow** - Programação reativa

## Configuração do Ambiente

### Pré-requisitos

- Android Studio Hedgehog | 2023.1.1 ou superior
- Kotlin 2.0.21 ou superior
- Android SDK API 24+ (Android 7.0)
- Target SDK API 36 (Android 14)

### Instalação

1. Clone o repositório:
```bash
git clone [URL_DO_REPOSITORIO]
cd Dormindo
```

2. Abra o projeto no Android Studio

3. Sincronize o projeto com os arquivos Gradle

4. Execute o aplicativo em um emulador ou dispositivo físico

## Estrutura do Projeto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/dormindo/
│   │   │   ├── data/           # Camada de dados
│   │   │   ├── domain/         # Camada de domínio
│   │   │   ├── presentation/   # Camada de apresentação
│   │   │   └── di/            # Injeção de dependências
│   │   └── res/               # Recursos do Android
│   ├── test/                  # Testes unitários
│   └── androidTest/           # Testes de UI
├── build.gradle.kts           # Configuração do módulo
└── proguard-rules.pro        # Regras do ProGuard
```

## Arquitetura

O projeto segue os princípios da Clean Architecture com MVVM:

- **Presentation Layer**: UI com Jetpack Compose e ViewModels
- **Domain Layer**: Use Cases e entidades de negócio
- **Data Layer**: Repositórios e fontes de dados
- **Infrastructure Layer**: Implementações concretas

## Funcionalidades

- [ ] Monitoramento de sono
- [ ] Análise de padrões
- [ ] Relatórios e estatísticas
- [ ] Configurações personalizadas
- [ ] Notificações e lembretes

## Desenvolvimento

### Branches

- `main` - Branch principal com código estável
- `develop` - Branch de desenvolvimento
- `feature/*` - Branches para novas funcionalidades
- `hotfix/*` - Branches para correções urgentes

### Commits

Siga o padrão Conventional Commits:

```
feat: adiciona nova funcionalidade
fix: corrige bug
docs: atualiza documentação
style: formatação de código
refactor: refatoração
test: adiciona ou corrige testes
chore: tarefas de manutenção
```

## Testes

- **Testes Unitários**: JUnit 4
- **Testes de UI**: Espresso e Compose Testing
- **Cobertura**: Meta de 80%+

## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

**Desenvolvido com ❤️ usando CursorRIPER Framework** 