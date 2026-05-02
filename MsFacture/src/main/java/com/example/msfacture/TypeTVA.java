package com.example.msfacture;

public enum TypeTVA {

        TVA_7(0.07),
        TVA_19(0.19);

        private final double taux;

        TypeTVA(double taux) {
            this.taux = taux;
        }

        public double getTaux() {
            return taux;
        }
    }


