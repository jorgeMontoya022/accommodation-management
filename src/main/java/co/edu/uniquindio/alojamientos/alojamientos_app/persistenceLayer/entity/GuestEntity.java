package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuestEntity extends UserEntity{

    //Relación One-to-Many: Un huésped tiene muchas reservas
    @OneToMany(mappedBy = "guestEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingEntity> bookingEntityList = new ArrayList<>();


    @OneToMany(mappedBy = "authorGuest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommentEntity> commentsWritten = new ArrayList<>();




}
