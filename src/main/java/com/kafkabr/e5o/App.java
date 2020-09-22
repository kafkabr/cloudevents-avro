package com.kafkabr.e5o;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.kafka.serializers.KafkaAvroSerializer;

public class App {

    public static void main(String[] args) {



      Properties configs = new Properties();
      configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
          StringSerializer.class);

      configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
          KafkaAvroSerializer.class);

      configs.put("specific.avro.reader", Boolean.TRUE);
      configs.put("schema.registry.url",
              "http://configure-me-schema-registry.hi");

      configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
              "configure-me-kafka.hi:9042");
      configs.put(ProducerConfig.CLIENT_ID_CONFIG, "cloudevents");

      // ####
      // data
      DebitoExecutado debito = DebitoExecutado.newBuilder()
        .setValor(-45.89)
        .setConta("234559")
        .setDescricao("Pagamento de Impostos")
        .build();

      ProducerRecord<String, DebitoExecutado> registro =
        new ProducerRecord<>("transacoes", debito.getConta(), debito);

      // ####
      // Modo Binário, onde os atributos CloudEvents seguem no cabeçalho
      registro.headers().add("ce_type",
              "ml.kafka.tef.debito.v1".getBytes());

      registro.headers().add("ce_id",
              "a4c15cfb-dc65-4215-9281-aa5fd50201ab".getBytes());

      registro.headers().add("ce_source", "https://kafka.ml/tef".getBytes());

      registro.headers().add("ce_subject", "890344".getBytes());

      registro.headers().add("ce_time", "2020-09-18T05:31:00Z".getBytes());

      registro.headers().add("ce_specversion", "1.0".getBytes());

      registro.headers().add("content-type", "application/avro".getBytes());

      try(KafkaProducer<String, DebitoExecutado> producer =
            new KafkaProducer<>(configs)){

          // ####
          // Produzir evento
          producer.send(registro);
      }
    }
}
