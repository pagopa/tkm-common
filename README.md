## Description

Tkm-common contains resources, classes and annotations shared between microservices

## How to use

 1. include the pom dependency:
> 
    <groupId>it.gov.pagopa.tkm</groupId>
    <artifactId>common</artifactId>
    <version>version</version>

### For the custom formatter annotations
 1. Import the configuration class in the main class of the microservice
 > @Import(CustomAnnotation.class)
 
 2. use it
 >  @StringFormat(StringFormatEnum.UPPERCASE)
 >  @StringFormat(StringFormatEnum.LOWERCASE)
 
 ### For the custom check annotations
  1. you can use it with target class
  > @CheckAtLeastOneNotEmpty(fieldNames = {"field1", "field2",...,"fieldn"})

  1. Import the configuration class in the main class of the microservice
  
  2. include in the application.yml
  > keyvault:
      readQueuePrvPgpKey: YOUR_VALUE
      readQueuePubPgpKey: YOUR_VALUE
      readQueuePrvPgpKeyPassphrase: YOUR_VALUE (default value 'null')
      
  2. use it