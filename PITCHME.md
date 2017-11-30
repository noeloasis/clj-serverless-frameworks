---

# clj/cljs向けServerlessソリューション

- shibuya.lisp #58 2017/11/30
- 株式会社シグニファイア代表 中村研二 (github: k2n, twitter: @k2nakamura)

---

## 発表者について

- 93-00年 野村総合研究所、00-16年 米国スタートアップ数社で勤務
- 15年、200億円規模の米国証券バックオフィスシステムにClojureを適用
    - compojure-api, core.async, aleph, manifold, gloss, mysql, mongo db, docker, AWS
- 16年、Clojure+Docker+Micro Services+AWSでビスポーク開発を提供する株式会社シグニファイアを設立
- 16年、たばこ卸売業向けEコマースとたばこ税申告システム
    - AWS Lambda, Incanter, compojure-api, clara-rules, reagent, postgresql, docker, rancher, AWS
- 17年、グローバル法律事務所紹介ネットワークシステムでLegalWeek Innovation Awardsを受賞
    - reagent, re-frame, compojure-api, elastic search, postgresql, docker, rancher, AWS

---

## Serverlessとは？

- ステートレスなコンピューティングコンテナ上で動作 |
- イベント駆動型である |
- Ephemaral（非永続的）である |
- Function as Service (FaaS) |
- AWS Labmda, Google Cloud Functions, Azure Fnctions, etc. |

---

## AWS Lambda

- AWS Lambdaは下記のイベントをトリガーとして、Java, Python, Node.js, C#で記述された関数を実行する。
    - API Gateway
    - S3へのPUT, POST, COPY, DELETE
    - Dynamo DBのtrigger
    - Kinesis Streams(AWS版Apache Kafka)のpoll
    - SNS(PubSub), SES(Eメール), 
    - Cognito(認証)
    - CloudWatch
    - CodeCommit, Alexa, Lex, などなど
- AWS Lambdaコンソール |

--- 

## Lambdaの適用例

- 月次処理でCSVデータからExcelファイルを生成
- [incanter](https://github.com/incanter/incanter)でデータ処理
- [mjul/docjure](https://github.com/mjul/docjure)でExcelファイルを生成
- ひと月あたり、100万リクエスト、メモリ1GBで約111コンピューティング時間まで無料！（12ヶ月の無料利用枠期間終了後も！）
- 今月は95ファイルを生成
- ファイル生成にかかる費用は無料
- 過去２年分のファイルをS3に保管するコストは約9円!
    
---

## AWS Gateway

- API GatewayはAWS Cloud上でRESTfulエンドポイントを提供する手段
- CloudFrontと連携し、edge networkとcacheによる低レイテンシー |
- 複数バージョンAPIの同時稼働によるテストとデプロイの効率化 |
- Cognito, OAuthと連携したセキュリティ |
- AWS API Gatewayコンソール | 

---
## clj/cljsをLambdaに対応させるアプローチ

- cljをAOTして、com.amazonaws.services.lambda.runtime.RequestHandlerの実装を生成し、Javaプラットフォームにデプロイ
- cljsをGoogle Closureコンパイラでjsに変換し、node.jsプラットフォームにデプロイ
- [Lambda上でのclj/cljsパフォーマンス比較](https://numergent.com/2016-01/AWS-Lambda-Clojure-and-ClojureScript.html)
- コールドインスタンス問題はCloud Watch Schedulerから定期的にリクエストを送ることで回避可能
- コードサイズ（含む依存関係のjar)が圧縮時50MB、展開時250MBであることに注意！
    - [AWS Lambdaの制限](http://docs.aws.amazon.com/ja_jp/lambda/latest/dg/limits.html)

---
## uswitch/Lambada 

- [uswitch/lambada](https://github.com/uswitch/lambada)
- com.amazonaws.services.lambda.runtime.RequestHandlerの実装を生成するマクロ

```clojure
(ns cigar-tax-report-aggregator.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [uswitch.lambada.core :refer [deflambdafn]]))

(defn handle-event
  [event]
  (log/debug "Got the following event: " (pr-str event))
  ;; parse the event and dispatch based on the event
  {:status "ok"})

(deflambdafn cigar-tax-report-aggregator.core.LambdaFn
  [in out ctx]
  (try (let [event (json/read (io/reader in))
             res (handle-event event)]
         (with-open [w (io/writer out)]
           (json/write res w)))
       (catch Throwable e
         ;; send error to SNS topic
         (throw e))))
```

@4
@[10-17]
@[12-20]
@[6-10]

---

## Lambdaへのデプロイメント

- [mhjort/clj-lambda-utils](https://github.com/mhjort/clj-lambda-utils)

```clojure
(defproject cigar-tax-report-generator "0.1.0-SNAPSHOT"
...
  :lambda {"dev" [{:handler "cigar-tax-report-aggregator.core.LambdaFn"
                   :memory-size 1024
                   :timeout 240
                   :function-name "aggregate-sales"
                   :region "ap-northeast-1"
                   :s3 {:bucket "dev.lambda-jars"
                        :object-key "cigar-tax-report-aggregator.jar"}}]
           "release" [{:handler "cigar-tax-report-aggregator.core.LambdaFn"
                   :memory-size 1024
                   :timeout 240
                   :function-name "aggregate-sales"
                   :region "ap-northeast-1"
                   :s3 {:bucket "lambda-jars"
                        :object-key "cigar-tax-report-aggregator.jar"}}]}
...
                :plugins [[com.jakemccrary/lein-test-refresh "0.15.0"]
                          [lein-clj-lambda "0.4.0"]]}}
```

@[3-9]
@19

---

## Clojure

---
## １エンドポイント毎に１関数を割り当てるのは面倒...

- 
- [jpb/ring-aws-lambda-adapter](https://github.com/jpb/ring-aws-lambda-adapter)
    - AWS Lambdaエントリポイントとringを連携させる 
- [mhjort/ring-apigw-lambda-proxy](https://github.com/mhjort/ring-apigw-lambda-proxy)
    - Lambadaとringを連携させるring middleware

---

## ClojureをAPI Gateway/Lambdaにデプロイするソリューション

- [clj-lambda-utils](https://github.com/mhjort/clj-lambda-utils)
    - JarをS3にアップロードし、Lambdaにデプロイするソリューション。API Gatewayへのデプロイも可能。
- [apex/apex](https://github.com/apex/apex) 
    - Servlessとは対象的に、AWS Lambdaに特化して管理するシステム。
    - node.jsから子プロセスを起動することで、 Golangなど、標準ではサポートされない言語も利用できる。
    - Clojureのデプロイをサポート。(のはずだが、サンプルが動かない...) 
    - apexはAPI Gatewayへのデプロイを直接サポートしてないので、[apex-api-gateway](https://www.npmjs.com/package/apex-api-gateway)などを利用。

---

## cljsからAPI Gateway+Labmdaへデプロイ
- [nervous-systems/cljs-lambda](https://github.com/nervous-systems/cljs-lambda) |
    - cljsをAWS Lambdaにデプロイするソリューション。API Gatewayへのデプロイはなし |
- [nervous-systems/serverless-cljs-plugin](https://github.com/nervous-systems/serverless-cljs-plugin)
    - プラットフォームを抽象化し、AWS Lambda, Azure Functions, Google CloudFunctionsをサポートするServerlessにcljsをデプロイするプラグイン |
    - ServerlessはLambdaとAPI Gatewayまでの登録を一気に行う |
    - デモ |

---

## ClojureからAPI Gateway+Labmdaへデプロイ

- [portkey-cloud/portkey](https://github.com/portkey-cloud/portkey)
     - デモ

---

## Portkeyのヤバさ

- REPLから直接デプロイできる。
    - プラグインも、serverlessやapexのようなデプロイ用のフレームワークも必要なし
    - データ => プログラム => デプロイ の流れがプログラムで実現可能 (Infrastructure as Code)
- ツリー・シェイキングによるデプロイコードのスリム化
    - 詳細は[スライド](https://github.com/portkey-cloud/portkey-clojutre-2017/blob/master/Portkey%20ClojuTRE%202017.pdf)と[動画](https://www.youtube.com/watch?v=qJXqQATJNTk&list=PLetHPRQvX4a9iZk-buMQfdxZm72UnP3C9&index=6)で。

---