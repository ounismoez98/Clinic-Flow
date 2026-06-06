package tn.esprit.spring.patientmedcin;

import java.util.Map;

/** Aggregated numbers for the Doctors dashboard. */
public class MedecinStats {
    private long total;
    private long available;
    private long inConsultation;
    private long offDuty;
    private double avgExperience;
    private Map<String, Long> bySpecialty;

    public MedecinStats() {
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getAvailable() { return available; }
    public void setAvailable(long available) { this.available = available; }

    public long getInConsultation() { return inConsultation; }
    public void setInConsultation(long inConsultation) { this.inConsultation = inConsultation; }

    public long getOffDuty() { return offDuty; }
    public void setOffDuty(long offDuty) { this.offDuty = offDuty; }

    public double getAvgExperience() { return avgExperience; }
    public void setAvgExperience(double avgExperience) { this.avgExperience = avgExperience; }

    public Map<String, Long> getBySpecialty() { return bySpecialty; }
    public void setBySpecialty(Map<String, Long> bySpecialty) { this.bySpecialty = bySpecialty; }
}
