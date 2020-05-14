# Programme Scala pour exporter des données d'une base MongoDB vers un fichier Excel
Programme Scala pour exporter des données d'une base MongoDB vers un fichier Excel

## Utilisation:
```
java -jar ExportExcelia.jar [-db db] [[-b début] [-e fin]|-n nbJour]
		[-cc callCenter] [-client client|-clientUuid uuid]
		[-p path] [-f fichier] [-s suffixe] [-d] [-t]
```
où :
* ```-db db``` est la référence à la base de données, par défaut désigne la base de données de développement. Voir fichier *ExportExcelia.prop* (optionnel).
* ```-b début``` est la date de début de l'extraction à 0h au format JJ/MM/AAAA. Amorcée à une semaine en arrière par défaut (paramètre optionnel).
* ```-e fin``` est la date de fin de l'extraction à 0h au format JJ/MM/AAAA. Amorcée à aujourd'hui par défaut (paramètre optionnel).
* ```-n nbJour``` précise le nombre de jour(s) à compter de la date courante. Désactivé par défaut (paramètre optionnel).
* ```-cc callCenter``` est la référence au centre d'appel. Armorcée à Excelia par défaut (paramètre optionnel).
* ```-client client``` est la référence au client servant à filtrer les résultats. Désactivé par défaut (paramètre optionnel).
* ```-clientUuid uuid``` est l'identifiant unique du client servant à filtrer les résultats. Désactivé par défaut (paramètre optionnel).
* ```-p path``` est le répertoire vers lequel exporter les fichiers contenant les résultats. Par défaut c'est le répertoire courant du programme (paramètre optionnel).
* ```-f ficher``` est le fichier Excel qui recevra les résultats. Par défaut, c'est le fichier *Export.xlsx* (paramètre optionnel).
* ```-s suffixe``` est le suffixe à ajouter au nom du fichier. Par défaut il n'y a pas de suffixe (paramètre optionnel).
* ```-d``` le programme s'exécute en mode débug, il est beaucoup plus verbeux. Désactivé par défaut (paramètre optionnel).
* ```-t``` le programme s'exécute en mode test, les transactions en base de données ne sont pas faites. Désactivé par défaut (paramètre optionnel).

## Pré-requis :
- Scala 2.13.2 ou supérieur
- Java 14 ou supérieur.
- Driver MongoDb

## Fichier des paramètres : 
Ce fichier permet de spécifier les paramètres d'accès aux différentes bases de données.

A adapter selon les implémentations locales.

Ce fichier est nommé : *ExportExcelia.prop*.

Le fichier *ExportExcelia_Example.prop* est fourni à titre d'exemple.

## Fichier Excel :
Ce fichier est créé à la main. Il doit pré-exister à toute exécution du programme.
Il contient autant d'onglets qu'il y a de table/collection à exporter.

## Références :
### Scala
- [A tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html)
- [Scala Book](https://docs.scala-lang.org/overviews/scala-book/introduction.html)
- [Jenkov's Scala tutorial (version2.8.0)](http://tutorials.jenkov.com/scala/index.html)
- [Data Flair Scala tutorials](https://data-flair.training/blogs/scala-tutorials-home/)
- [Javatpoint Scala tutorial](https://www.javatpoint.com/scala-tutorial)
- [10 Essential Tips and Tricks For IntelliJ IDEA](https://www.youtube.com/watch?v=Mr2mPu1tLhk)
- [Try, Option or Either ?](https://xebia.com/blog/try-option-or-either/)
- [Easy validation with Sacal's Either](https://coderwall.com/p/kokm7w/easy-validation-with-scala-s-either)
- [The Neophyte's guide to Scala part 7](https://danielwestheide.com/blog/the-neophytes-guide-to-scala-part-7-the-either-type/)
### MongoDb
- [MongoDb with Scala quick tour](http://mongodb.github.io/mongo-scala-driver/2.2/getting-started/quick-tour/)
- [How Scala interacts with MongoDB](https://blog.knoldus.com/how-scala-interacts-with-mongodb/)
### Excel
### Lire les paramètres en ligne de commande
- [Best ways to parse command-line parameters in Scala](https://stackoverflow.com/questions/2315912/best-way-to-parse-command-line-parameters)