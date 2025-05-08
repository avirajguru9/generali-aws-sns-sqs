package com.generali.aws.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Policy {
	 private Long id;
	 private String policyNumber;
	 private String policyHolderName;
	 private int policyTerm;
	 private double coverageAmount;
	 private double premium;
	 private String policyType;
}
