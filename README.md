## How to use

 1. include the pom dependency:
> 
    <groupId>it.gov.pagopa.tkm</groupId>
    <artifactId>common</artifactId>
    <version>version</version>

 1. Import the configuration class
 > @Import(CustomAnnotation.class)
 
 2. use it
 >  @StringFormat(UPPERCASE)
 >  @StringFormat(lower)