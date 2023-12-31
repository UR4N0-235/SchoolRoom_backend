package com.ur4n0.schoolroom.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ur4n0.schoolroom.person.PersonModel;
import com.ur4n0.schoolroom.person.PersonService;
import com.ur4n0.schoolroom.room.RoomModel;
import com.ur4n0.schoolroom.room.RoomService;
import com.ur4n0.schoolroom.utils.TimeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private PersonService personService;

    @Autowired
    private RoomService roomService;

    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        Optional<PersonModel> person = personService.getPersonById(scheduleDTO.getPersonId());
        Optional<RoomModel> room = roomService.getRoomById(scheduleDTO.getRoomId());

        if (!person.isPresent() && !room.isPresent())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Não existe professor nem sala com o id informado!");
        if (!person.isPresent())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("não existe professor com o id informado!");
        if (!room.isPresent())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("não existe sala com o id informado!");

        ScheduleModel schedule = new ScheduleModel();
        schedule.setStartDate(TimeConverter.convertTimestampToDateTime(scheduleDTO.getStartDate()));
        schedule.setEndDate(TimeConverter.convertTimestampToDateTime(scheduleDTO.getEndDate()));
        schedule.setPerson(person.get());
        schedule.setRoom(room.get());

        if (!scheduleService.isUnoccupiedRoom(schedule))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Esta sala ja esta ocupada nesse horario!");

        ScheduleModel createdSchedule = scheduleService.createSchedule(schedule);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleModel> updateSchedule(@PathVariable Long id,
            @RequestBody ScheduleModel updatedSchedule) {
        try {
            ScheduleModel schedule = scheduleService.updateSchedule(id, updatedSchedule);
            return new ResponseEntity<>(schedule, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleModel> getScheduleById(@PathVariable Long id) {
        Optional<ScheduleModel> schedule = scheduleService.getScheduleById(id);
        if (schedule.isPresent()) {
            return new ResponseEntity<>(schedule.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping()
    public ResponseEntity<List<ScheduleModel>> getAllSchedules() {
        List<ScheduleModel> schedules = scheduleService.getAllSchedules();
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    // GET params

    @GetMapping(params = "personId")
    public ResponseEntity<List<ScheduleModel>> getSchedulesByPerson(@RequestParam("personId") Long personId) {
        Optional<PersonModel> person = personService.getPersonById(personId);

        if (person.isPresent()) {
            List<ScheduleModel> schedules = scheduleService.getSchedulesByPerson(person.get());
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(params = "roomId")
    public ResponseEntity<List<ScheduleModel>> getSchedulesByRoom(@RequestParam() Long roomId) {
        Optional<RoomModel> room = roomService.getRoomById(roomId);

        if (room.isPresent()) {
            List<ScheduleModel> schedules = scheduleService.getSchedulesByRoom(room.get());
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(params = "day")
    public ResponseEntity<List<ScheduleModel>> getSchedulesByDay(@RequestParam() String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        List<ScheduleModel> schedules = scheduleService.getSchedulesByDay(parsedDate);
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping(params = { "startDay", "endDay" })
    public ResponseEntity<List<ScheduleModel>> getSchedulesByDayRange(@RequestParam() String startDate,
            @RequestParam() String endDate) {
        LocalDate parsedStartDate = LocalDate.parse(startDate);
        LocalDate parsedEndDate = LocalDate.parse(endDate);
        List<ScheduleModel> schedules = scheduleService.getSchedulesByDayRange(parsedStartDate, parsedEndDate);
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping(params = { "startDateTime",
            "endDateTime" })
    public ResponseEntity<List<ScheduleModel>> getSchedulesByDateTimeRange(
            @RequestParam() String startDateTime,
            @RequestParam() String endDateTime) {
        LocalDateTime parsedStartDateTime = LocalDateTime.parse(startDateTime);
        LocalDateTime parsedEndDateTime = LocalDateTime.parse(endDateTime);
        List<ScheduleModel> schedules = scheduleService.getSchedulesByDateAndTimeRange(parsedStartDateTime,
                parsedEndDateTime);
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping(params = { "personId",
            "roomId" })
    public ResponseEntity<List<ScheduleModel>> getSchedulesByPersonAndRoom(
            @RequestParam() Long personId,
            @RequestParam() Long roomId) {
        Optional<PersonModel> person = personService.getPersonById(personId);
        Optional<RoomModel> room = roomService.getRoomById(roomId);

        if (person.isPresent() && room.isPresent()) {
            List<ScheduleModel> schedules = scheduleService.getSchedulesByPersonAndRoom(person.get(), room.get());
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
