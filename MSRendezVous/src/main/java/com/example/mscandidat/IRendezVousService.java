package com.example.mscandidat;

import java.util.List;

public interface IRendezVousService {

    public List<RendezVous> getAll();
    RendezVous addRendezVous(RendezVous rendezvous);
    RendezVous updateRendezVous(int id, RendezVous rendezvous);
    boolean deleteRendezVous(int id);
    RendezVous getRendezVous(int id);

}
