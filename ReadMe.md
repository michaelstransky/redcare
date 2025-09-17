# Backend Coding Challenge

## Project overview:

The objective of this project is to implement a backend application for scoring repositories on GitHub.

## Initial Information:

GitHub provides a public search endpoint which you can use for fetching repositories. You can find the
documentation [here](https://docs.github.com/en/rest/search/search?apiVersion=2022-11-28#search-repositories).
The user should be able to configure the earliest created date and language of repositories.

## Task: Popularity Score Assignment

* Develop a scoring algorithm that assigns a popularity score to each repository.
* Factors contributing to the score include stars, forks, and the recency of updates.

## Important

We value concise and clean code, documentation, scalability, performance, and testing.

# Solution

## Design decisions

### Scoring function

Stars, forks, update recency have different scales.
A perfect scoring function may contain getting the quantile of each property, making the scoring of each entry dependent
on all others, and multiplying them to get a score in the range [0,1].
If, for comparison, the whole database instead of only the requested entries were used, preprocessing would be easily
possible.  
However, for simplicity and performance, this approach multiplies weighted sums, keeping in mind that every scoring
should be easily exchangeable.

### Input parameter

#### Language

Projects can have multiple programming languages, but GitHub's API returns only the most used.
Thus, there is no risk of data corruption by relying on _the_ language of a GitHub repository.   
The search for programming languages is case-insensitive to avoid DOS attacks using, e.g. _Java_, _JaVa_, _java_, etc.

#### Creation Date

GitHub's API allows local dates and timestamps for searching.
This API will only allow timestamps to avoid confusion (for the user).

### Performance

As the API for getting info about GitHub repositories cannot provide rates necessary for synchronous replays.  
As the score depends on the update recency, and stars and forks can change any time, the scores cannot be cached.

Possible solutions are:

* Always asynchronous handling
* Caching the information needed for scoring GitHub repositories

#### Always asynchronous handling

Reply _204 CREATED_ for a request together with a UUID for the job to be processed.
The job would be kept track of in a database, making the status returnable and, optionally, the job cancellable.
This requires querying another endpoint with the UUID to see when the job is done and getting the information finally
from there.
This creates an enormous overhead, as similar jobs require the same, probably not yet changed information to be
requested over and over again via the slow GitHub API.

#### Caching the information

The information needed for scoring instead of the scores themselves will be cached.
However, the information cannot be cached arbitrarily long, as stars and forks and last update can change any time.  
Thus, the information needs to be permanently updated by independent services.
Given the rate limit of GitHub's API, this requires a lot of independent services and risks getting the IP range banned.

#### Pragmatic approach taken

The information needed for scoring will be obtained asynchronously and then cached,
providing a synchronous reply for future queries already in the cached time range.
If a query contains partly already cached information, only the information with creation dates older than available is
obtained.
As at some point the cached information would be considered too old, the age of each cached time range needs to be
tracked and re-obtained when getting too old.
For simplicity, the cache is always assumed to be recent, and items newer than the first cached query will not be
returned, as the quality of very recent repositories is questionable anyway.
The system is assumed to be reset, e.g., by restarting on a daily basis to clean the cache.

As querying is cheap, there is a single endpoint replying  _202 ACCEPTED , If information has to be obtained - come back
later.
If all information is already available, the reply is _200 OK_ with the name and score of the requested GitHub
repositories.

As users may try again at a high rate, the REST service caches if information is already requested and does not
commission the obtaining again.

### Scalability

Scalability is usually meant as the number of users who can query a service simultaneously.
This is easily possible, as multiple REST endpoints can be deployed and the database for caching could be upgraded to a
distributed cache.
The problem in this task is the rate-limited GitHub API.
In this solution the REST endpoint(s) sends messages through RabbitMQ for timeranges to be obtained.  
The multiple consumer(s) of the requests _extractor_s, which call the GitHub API, can be deployed (for future works via
Kubernetes via autoscaling) for easy scaling up of the time-consuming process of GitHub API calls.
The GitHub API is rate limited for anonymous users per IP and for authenticated users per account.
Multiple extractors thus need different credentials, which can be easily set via environment.  
For simplicity, this implementation uses 1 anonymous account and thus 1 extractor.

## Implementation

Technologies used:

* Spring Boot with JPA
* RabbitMQ
* PostgreSQL
* Docker
* OpenApi
* REST Assured
* Mockito
* Junit
* Java 21
* OpenApi with swagger

For GitHub API calls,
the [unofficial but officially listed Java API](https://docs.github.com/en/rest/using-the-rest-api/libraries-for-the-rest-api?apiVersion=2022-11-28#java)
is used, as I did not assume this to be a test on how to use a RestClient or Spring WebClient or RestTemplate.

The whole project, in a version with 1 endpoint and 1 extractor, is ready to be deployed via Docker, as Java 21 may not
be available on all machines and to make cloud deployment easy.

## How to use

### setup

Set required parameters and especially POSTGRES_PASSWORD in [.env](.env).
Then start the dockerized setup.
On linux systems simply:

```shell
POSTGRES_PASSWORD="$(openssl rand -base64 48)" envsubst < template.env > .env
docker compose up -d
```

### Usage

Use [OpenApi](http://localhost:8080/swagger-ui.html) for conveniently testing this API.

### Testing

Have a RabbitMQ and PostgreSQL running, e.g. via

```shell
docker compose -f docker-compose-only-infra.yml up -d
```

deploy the interfaces via mvn

```shell
cd infterfaces ; mvn install
```

run endpoint and extractor in your IDE with the environment variable _POSTGRES_PASSWORD_ set.
``