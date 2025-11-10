package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    //NOTE:: here we do not need @Autowire because after spring 4.3 if you have only one constructor
    //       in class then no need to specify it with @Autowire.
    private PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getAllPatients() {
       List<Patient> patients =  patientRepository.findAll();

       List<PatientResponseDTO> patientResponseDTOS = patients.stream()
               .map(patient -> PatientMapper.toDTO(patient)).toList();
       return patientResponseDTOS;
    }

    public  PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistException("A patient with this email" + "already  exists" + patientRequestDTO.getEmail());
        }
        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));
        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
       Patient patient =  patientRepository.findById(id).orElseThrow(()->new PatientNotFoundException("Patient Not Found with id:"+ id));

       //NOTE:: while updating if email and id are already matched from DB then no problem (means we are updating entry with same email)
        //      but if id and email are different then throw error(means email already exists with another id/entry).
       if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),id)) {
            throw new EmailAlreadyExistException("A patient with this email" + "already  exists" + patientRequestDTO.getEmail());
       }

       patient.setName(patientRequestDTO.getName());
       patient.setEmail(patientRequestDTO.getEmail());
       patient.setAddress(patientRequestDTO.getAddress());
       patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

       Patient updatedPatient = patientRepository.save(patient);
       return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }
}
