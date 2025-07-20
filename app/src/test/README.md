# Testes Unitários - Dormindo App

Este diretório contém os testes unitários para o aplicativo Dormindo, implementados seguindo as melhores práticas de Clean Architecture e Test-Driven Development.

## Estrutura dos Testes

### 1. Testes de Entidades (Domain Layer)

#### `TimerTest.kt`
- Testa a criação de timers com diferentes durações
- Verifica a geração de IDs únicos
- Testa igualdade e hashCode
- Valida valores padrão

#### `TimerStatusTest.kt`
- Testa todos os estados do timer (Idle, Running, Paused, Completed, Error)
- Verifica igualdade entre estados
- Testa hashCode para diferentes estados

#### `MediaInfoTest.kt`
- Testa criação de informações de mídia
- Verifica campos obrigatórios e opcionais
- Testa igualdade e hashCode

#### `MediaPlaybackStateTest.kt`
- Testa todos os estados de reprodução (Playing, Paused, Stopped, NoMedia)
- Verifica igualdade entre estados
- Testa toString para cada estado

### 2. Testes de Use Cases (Domain Layer)

#### `StartTimerUseCaseTest.kt`
- Testa início de timer com mídia tocando
- Testa falha quando não há mídia
- Testa falha do repositório
- Verifica chamadas corretas aos repositórios

#### `StopTimerUseCaseTest.kt`
- Testa parada de timer com sucesso
- Testa falha ao parar timer
- Testa falha ao parar mídia
- Verifica sequência de chamadas

#### `GetTimerStatusUseCaseTest.kt`
- Testa obtenção de todos os status possíveis
- Verifica retorno correto dos dados
- Testa diferentes estados do timer

### 3. Testes de Repositórios (Domain Layer)

#### `TimerRepositoryTest.kt`
- Testa todas as operações CRUD do timer
- Verifica sucesso e falha de operações
- Testa observação de status
- Testa verificação de timer ativo

#### `MediaRepositoryTest.kt`
- Testa controle de reprodução de mídia
- Verifica obtenção de informações de mídia
- Testa observação de estado de reprodução
- Testa diferentes cenários de mídia

###4estes de ViewModel (Presentation Layer)

#### `TimerViewModelTest.kt`
- Testa inicialização do ViewModel
- Testa início e parada de timer
- Testa atualização de informações de mídia
- Testa tratamento de erros
- Testa limpeza de estado
- Testa observação de mudanças
- Testa atualização de segundos restantes

### 5stes de Integração

#### `IntegrationTest.kt`
- Testa fluxo completo de timer
- Verifica integração entre componentes
- Testa cenários de erro end-to-end
- Testa observação de mudanças de estado

## Tecnologias Utilizadas

- **JUnit4ramework de testes
- **MockK**: Biblioteca de mocking para Kotlin
- **Kotlin Test**: Assertions adicionais
- **Coroutines Test**: Testes de coroutines (TestCoroutineDispatcher)
- **Koin Test**: Injeção de dependência para testes

## Padrões de Teste

### Estrutura AAA (Arrange-Act-Assert)
Todos os testes seguem o padrão AAA:
```kotlin
@Test
fun `descrição do teste`() = runBlockingTest {
    // Arrange - Preparar dados e mocks
    val expectedValue =valor esperado"
    coEvery { repository.method() } returns expectedValue
    
    // Act - Executar ação
    val result = useCase.execute()
    
    // Assert - Verificar resultado
    assertEquals(expectedValue, result)
    coVerify { repository.method() }
}
```

### Nomenclatura de Testes
Os testes usam nomenclatura descritiva em português:
- `deve [ação] quando [condição]`
- `quando [ação], deve [resultado esperado]`
- `nao deve [ação] se [condição]`

### Mocks e Stubs
- **MockK** para criar mocks de dependências
- **relaxed = true** para mocks que não precisam de configuração específica
- **coEvery** para métodos suspend
- **every** para métodos síncronos
- **coVerify** para verificar chamadas de métodos suspend

### Testes de Coroutines
- **TestCoroutineDispatcher** para controlar execução de coroutines
- **runBlockingTest** para executar testes com coroutines
- **advanceUntilIdle()** para aguardar conclusão de coroutines
- **cleanupTestCoroutines()** para limpeza após testes

## Executando os Testes

### Executar todos os testes
```bash
./gradlew test
```

### Executar testes específicos
```bash
./gradlew test --testscom.example.dormindo.domain.usecase.StartTimerUseCaseTest"
```

### Executar testes com relatório
```bash
./gradlew test --info
```

## Cobertura de Testes

Os testes cobrem:
- ✅ Todas as entidades do domínio
- ✅ Todos os use cases
- ✅ Interfaces dos repositórios
- ✅ ViewModel principal
- ✅ Fluxos de integração
- ✅ Cenários de sucesso e erro
- ✅ Estados diferentes do timer
- ✅ Estados diferentes de mídia

## Manutenção dos Testes

### Adicionando Novos Testes1 Crie o arquivo de teste no diretório apropriado2iga a nomenclatura existente3. Use MockK para mocks
4. Implemente o padrão AAA
5. Adicione documentação se necessário

### Atualizando Testes Existentes
1. Mantenha a estrutura AAA
2Atualize apenas as partes necessárias
3. Verifique se todos os testes ainda passam
4. Atualize a documentação se necessário

## Boas Práticas

1Isolamento**: Cada teste deve ser independente2 **Legibilidade**: Use nomes descritivos para testes3 **Simplicidade**: Um teste por cenário
4. **Manutenibilidade**: Evite duplicação de código
5. **Performance**: Use mocks apropriados6 **Documentação**: Comente casos complexos

## Troubleshooting

### Problemas Comuns

1. **MockK não encontrado**: Verifique se a dependência está no build.gradle.kts
2. **Coroutines não funcionam**: Use `runBlockingTest` e `TestCoroutineDispatcher`
3. **Mocks não funcionam**: Verifique se está usando `coEvery` para métodos suspend
4**Testes falham**: Verifique se os mocks estão configurados corretamente5dardTestDispatcher não encontrado**: Use `TestCoroutineDispatcher` para versões mais antigas do Kotlin

### Debug de Testes
```kotlin
// Adicione logs para debug
println("Estado atual: ${viewModel.uiState.value})println("Resultado: $result")
```

## Correções Recentes

### Versão 10.1✅ Substituído Mockito por MockK para melhor compatibilidade com Kotlin
- ✅ Corrigido imports de coroutines para usar TestCoroutineDispatcher
- ✅ Atualizado runTest para runBlockingTest
- ✅ Adicionado cleanupTestCoroutines() para limpeza adequada
- ✅ Corrigido advanceUntilIdle() para versão compatível 