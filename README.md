1) Clone project;
2) run 'docker compose up'
3) Access 'http://localhost:8080/swagger-ui/index.html'
4) To make a currency/currencies observable, invoke POST /currency method. Example of correct JSON:
```json
{
  "currencyCodes": [
    "USD","UAH","EUR"
  ]
}
```

Hint: Use this command `chmod +x ./gradlew`, if you're using Mac, and docker can't invoke gradle scripts because of lack of permissions. 
