##Description

Tkm-common contains resources, classes and annotations shared between microservices

## How to use

 1. include the pom dependency:
> 
    <groupId>it.gov.pagopa.tkm</groupId>
    <artifactId>common</artifactId>
    <version>version</version>

 1. Import the configuration class in the main class of the microservice
 > @Import(CustomAnnotation.class)
 
 2. use it
 >  @StringFormat(StringFormatEnum.UPPERCASE)
 >  @StringFormat(StringFormatEnum.LOWERCASE)