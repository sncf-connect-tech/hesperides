# Spring Boot Auto Configuration

Spring Boot détecte automatiquement les dépendances ajoutées dans les pom.xml et auto configure les beans associés tels ques les beans de connexions en base de données.

En ajoutant la dépendance suivante :
- spring-boot-starter-data-jpa
- spring-boot-starter-data-mongodb
- spring-boot-starter-data-elasticsearch

Spring active l'auto configuration des trois bases en parallèle ce qui n'est pas forcément le besoin de l'utilisateur.
L'idée est de tout désactiver et de réactiver une partie en fonction du choix entre ces trois bases.

Il est possible de désactiver l'auto configuration de certains beans de la manière suivante :

```
@SpringBootApplication
@EnableAutoConfiguration(
        exclude = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                ElasticsearchAutoConfiguration.class,
                ElasticsearchDataAutoConfiguration.class,
                ElasticsearchRepositoriesAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                MongoAutoConfiguration.class,
                MongoDataAutoConfiguration.class
        }
)
public class HesperidesSpringApplication {
```

Via les profiles spring, il est possible d'activer une partie de l'infrastructure (jpa / mongo / elasticsearch).
Dans certains packages de l'infrastucture nous réactivons juste les beans que nous souhaitons en réimportant les classes d'auto configuration que nous avons exclues plus tôt.

Exemple :

```
@Profile(Profiles.ELASTICSEARCH)
@Import({
        ElasticsearchAutoConfiguration.class,
        ElasticsearchDataAutoConfiguration.class
})
public class ElasticsearchConfiguration {
```