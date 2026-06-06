package tn.esprit.spring.patientmedcin;

import java.util.Map;

/** Aggregated numbers for the Patients dashboard. */
public class PatientStats {
    private long total;
    private long active;
    private long admitted;
    private long discharged;
    private long assigned;        // patients that have an assigned doctor
    private long unassigned;
    private Map<String, Long> byBloodType;

    public PatientStats() {
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getActive() { return active; }
    public void setActive(long active) { this.active = active; }

    public long getAdmitted() { return admitted; }
    public void setAdmitted(long admitted) { this.admitted = admitted; }

    public long getDischarged() { return discharged; }
    public void setDischarged(long discharged) { this.discharged = discharged; }

    public long getAssigned() { return assigned; }
    public void setAssigned(long assigned) { this.assigned = assigned; }

    public long getUnassigned() { return unassigned; }
    public void setUnassigned(long unassigned) { this.unassigned = unassigned; }

    public Map<String, Long> getByBloodType() { return byBloodType; }
    public void setByBloodType(Map<String, Long> byBloodType) { this.byBloodType = byBloodType; }
}
