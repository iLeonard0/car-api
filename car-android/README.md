# Integração de Autenticação e API REST no Aplicativo Android

Este projeto envolve a criação de uma tela de login com o Firebase, integração de uma API REST e a exibição de dados em um aplicativo Android. Siga as instruções abaixo para configurar e implementar as funcionalidades solicitadas.

### Requisitos

- **IDE**: Android Studio
- **Bibliotecas**: Firebase Authentication, Firebase Storage, Retrofit (para requisições REST).

## Instruções

### 1. Tela de Login com Firebase

Implemente uma tela de login que use um dos provedores de autenticação do Firebase (recomenda-se usar autenticação por telefone ou Google).

- **Autenticação por telefone**:
    - Configure o Firebase para aceitar o número de telefone: `+55 11 91234-5678`.
    - Defina o código de verificação para login de teste como `101010`.

- **Autenticação com Google** :
    - Habilite a autenticação com Google nas configurações do Firebase Console.
    - Implemente a lógica de autenticação no aplicativo, usando o provedor de login do Google.

### 2. Opção de Logout

Adicione uma opção de logout ao aplicativo, permitindo que o usuário saia da conta autenticada. Essa opção deve estar disponível em uma área visível, como o menu principal ou um botão específico na interface.

### 3. Integração com API REST `/car`

Implemente a integração com uma API REST disponível no [Link] (https://github.com/vagnnermartins/FTPR-Car-Api-Node-Express) para exibir e salvar informações de carros no aplicativo.

- **Estrutura JSON Esperada**:
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

- **Requisitos Específicos**:
    - O campo `imageUrl` deve apontar para uma imagem armazenada no Firebase Storage.
    - Exiba a imagem e as informações de cada carro no aplicativo.
    - Utilize Retrofit para realizar as requisições à API.


### Configuração do Projeto

1. **Firebase**: Configure o projeto com Firebase Authentication e Firebase Storage. Adicione o `google-services.json` ao projeto para integração.
2. **Dependências Gradle**:
    - **Firebase**: `Firebase Authentication` e `Firebase Storage`
    - **Retrofit** para a comunicação com a API REST
    - **Glide** ou outra biblioteca para carregamento de imagens

--- 
