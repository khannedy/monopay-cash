package com.monopay.wallet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopay.wallet.entity.Transfer;
import com.monopay.wallet.model.service.ListTransferServiceRequest;
import com.monopay.wallet.model.web.response.TransferWebResponse;
import com.monopay.wallet.repository.TransferRepository;
import com.monopay.wallet.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
public class TransferServiceImpl implements TransferService {

  @Autowired
  private TransferRepository transferRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Override
  public List<TransferWebResponse> list(@Valid ListTransferServiceRequest request) {
    return transferRepository.findAllByMemberIdAndMerchantIdOrderByCreatedAtDesc(request.getMemberId(), request.getMerchantId())
      .stream()
      .map(transfer -> TransferWebResponse.builder()
        .id(transfer.getId())
        .memberId(transfer.getMemberId())
        .bank(transfer.getBank())
        .bankAccountName(transfer.getBankAccountName())
        .total(transfer.getTotal())
        .status(transfer.getStatus())
        .createdAt(transfer.getCreatedAt())
        .build())
      .collect(Collectors.toList());
  }

  @Override
  public void publishTransfer(Transfer transfer) {
    try {
      String payload = objectMapper.writeValueAsString(transfer);
      kafkaTemplate.send("monopay-save-transfer-event", payload);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
  }
}
