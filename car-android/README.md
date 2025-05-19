# Integração de Autenticação e API REST no Aplicativo Android

Este projeto envolve a criação de uma tela de login com o Firebase, integração de uma API REST, exibição de dados em um aplicativo Android e integração com o Google Maps. Siga as instruções abaixo para configurar e implementar as funcionalidades solicitadas.

### Requisitos

- **IDE**: Android Studio
- **Bibliotecas**: Firebase Authentication, Firebase Storage, Retrofit (para requisições REST).

## Instruções

### 1. Tela de Login com Firebase

Autenticação por telefone ou Google).

- **Autenticação por telefone**:
    - Número: `+55 11 91234-5678`.
    - Código de verificação para login: `101010`.

- **Autenticação com Google** :
    - Utilize seu próprio gmail para logar

### 2. Integração com API REST `/car`

- **Estrutura JSON**:
    ```json
    {
      "imageUrl": "https://image",
      "year": "2020/2020",
      "name": "Gaspar",
      "licence": "ABC-1234",
      "place": {
        "lat": 0,
        "long": 0
      }
    }
    ```
