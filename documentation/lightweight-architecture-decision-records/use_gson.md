# Pourquoi Gson ?

Nous utilisons Gson pour sérialiser/désérialiser :
- les objets correspondant aux events du legacy
- les inputs de la couche présentation

Contrairement à Jackson, Gson est compatible avec les annotations Lombok
et nous permet de réduire la quantité de code à maintenir.
