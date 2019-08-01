package com.monopay.wallet.model.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListTransferServiceRequest {

  @NotBlank(message = "NotBlank")
  private String memberId;

  @NotBlank(message = "NotBlank")
  private String merchantId;
}
