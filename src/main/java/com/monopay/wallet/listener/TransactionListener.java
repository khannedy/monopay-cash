package com.monopay.wallet.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopay.wallet.entity.Transfer;
import com.monopay.wallet.entity.TransferStatus;
import com.monopay.wallet.event.SaveTransactionEvent;
import com.monopay.wallet.repository.TransferRepository;
import com.monopay.wallet.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class TransactionListener {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TransferService transferService;

  @Autowired
  private TransferRepository transferRepository;

  @KafkaListener(topics = "monopay-save-transaction-event")
  public void onSaveTransactionEvent(String payload) throws IOException {
    log.info(payload);
    SaveTransactionEvent event = objectMapper.readValue(payload, SaveTransactionEvent.class);

    if ("TRANSFER".equals(event.getType())) {
      if (!transferRepository.existsById(event.getId())) {
        Transfer transfer = Transfer.builder()
          .bank(event.getBank())
          .bankAccountName(event.getBankAccountNumber())
          .memberId(event.getBalanceId())
          .merchantId(event.getMerchantId())
          .status(TransferStatus.PENDING)
          .total(event.getBeforeBalance() - event.getAfterBalance())
          .build();

        transfer = transferRepository.save(transfer);

        transferService.publishTransfer(transfer);
      } else {
        Transfer transfer = transferRepository.findById(event.getId()).get();
        transfer.setBank(event.getBank());
        transfer.setBankAccountName(event.getBankAccountNumber());
        transfer.setMemberId(event.getBalanceId());
        transfer.setMerchantId(event.getMerchantId());
        transfer.setTotal(event.getBeforeBalance() - event.getAfterBalance());

        transfer = transferRepository.save(transfer);

        transferService.publishTransfer(transfer);
      }
    }
  }
}
