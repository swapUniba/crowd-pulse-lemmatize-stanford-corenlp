crowd-pulse-lemmatize-stanford-corenlp
======================================

Crowd Pulse lemmatizer implemented with Stanford CoreNLP.

--------------------------------------

The `lemmatizer-stanford` plugin needs additional lemmatization models in the classpath (see 
http://nlp.stanford.edu/software/corenlp.shtml).

You can specify the configuration option "calculate" with one of the following values:
- all: lemmatize all tokens coming from the stream;
- new: lemmatize the tokens with no lemma (property is null);

Example of usage:

```json
{
  "process": {
    "name": "lemmatizer-tester",
    "logs": "/opt/crowd-pulse/logs"
  },
  "nodes": {
    "message-fetcher": {
      "plugin": "message-fetch",
      "config": {
        "db": "test"
      }
    },
    "lemmatizer": {
      "plugin": "lemmatizer-stanford",
      "config": {
        "calculate": "new"
      }
    },
    "message-persister": {
      "plugin": "message-persist",
      "config": {
        "db": "test"
      }
    }
  },
  "edges": {
    "message-fetcher": [
      "lemmatizer"
    ],
    "lemmatizer": [
      "message-persister"
    ]
  }
}
```