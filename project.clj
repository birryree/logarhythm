(defproject logarhythm "0.1.0"
  :description "A toolkit for processing logs and integrating with services like Amazon Lambda"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.amazonaws/aws-lambda-java-core "1.1.0"]
                 [com.amazonaws/aws-lambda-java-events "1.1.0" :exclusions [[com.amazonaws/aws-java-sdk-s3]
                                                                            [com.amazonaws/aws-java-sdk-sns]
                                                                            [com.amazonaws/aws-java-sdk-kinesis]
                                                                            [com.amazonaws/aws-java-sdk-cognitoidentity]
                                                                            [com.amazonaws/aws-java-sdk-dynamodb]]]
                 [com.amazonaws/aws-lambda-java-log4j "1.0.0"]
                 [log4j/log4j "1.2.17"]
                 [org.clojure/tools.logging "0.3.1"]
                 [cheshire "5.5.0"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [semantic-csv "0.1.0"]
                 [amazonica "0.3.39" :exclusions [com.amazonaws/aws-java-sdk]]
                 [com.amazonaws/aws-java-sdk-core "1.10.37"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.37"]
                 [com.amazonaws/aws-java-sdk-sqs "1.10.37"]
                 [com.amazonaws/aws-java-sdk-cloudsearch "1.10.37"]
                 [com.amazonaws/aws-java-sdk-lambda "1.10.37"]]
  :java-source-paths ["src/java"]
  :aot :all
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

