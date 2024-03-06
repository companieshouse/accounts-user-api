# API Model Specification

## 1. Define API Model Spec
Your API model spec resides in `private.api.ch.gov.uk-specifications`. It serves as the authoritative source for your API, and any modifications should commence here, not within the codebase.

### 2. Generate DTOs and Controller Interface
Refer to the [private-api-sdk-java readme](https://github.com/companieshouse/private-api-sdk-java/blob/main/README.md) for instructions on generating your API resources from the spec.

### 3. Import DTOs
Include the following dependency in your POM file:
```xml
<dependency>
    <groupId>uk.gov.companieshouse</groupId>
    <artifactId>private-api-sdk-java</artifactId>
    <version>${private-api-sdk-java.version}</version>
</dependency>
```
to import your DTOs and any controller interface(s) into `accounts-user-api` from `private-api-sdk-java`. Note that these generated classes are stored in source control.


## 4. Write Your API Endpoints
If using a generated controller interface, you would need to implements it and override the endpoints.

### 5. Develop API Endpoints
Implement your API endpoints according to your requirements. If using a generated controller interface, ensure to implement it and override the necessary endpoints.

### 6. Generate Spec But Do NOT Overwrite
As you progress with endpoint development, consider generating your API spec locally from code using `source generate-spec.sh`. This allows you to compare the locally generated spec with the private repo spec. Manually update the private spec where required. Additional details on generating spec from code can be found [here](https://companieshouse.atlassian.net/wiki/spaces/IDV/pages/4301848621/Spike+-+How+to+generate+OpenAPI+spec+from+existing+code).



