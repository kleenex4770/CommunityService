package com.lth.community.service;

import com.lth.community.entity.BanMemberInfoEntity;
import com.lth.community.entity.DeleteMemberInfoEntity;
import com.lth.community.entity.FileInfoEntity;
import com.lth.community.repository.BanMemberInfoRepository;
import com.lth.community.repository.DeleteMemberInfoRepository;
import com.lth.community.repository.FileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@EnableScheduling
@SpringBootApplication
@RequiredArgsConstructor
@Transactional
public class DeleteAndRelease {
    private final BanMemberInfoRepository banMemberInfoRepository;
    private final DeleteMemberInfoRepository deleteMemberInfoRepository;
    private final FileRepository fileRepository;
    @Value("${file.files}") String path;
    @Scheduled(cron = "0 0 0 * * *")
    public void delete() {
        List<DeleteMemberInfoEntity> delete = deleteMemberInfoRepository.findAll();
        if(delete.size() == 0) {
            System.out.println("삭제할 회원이 없습니다.");
            return;
        }
        for(int i=0; i<delete.size(); i++) {
            if(delete.get(i).getDeleteDt().equals(LocalDate.now())) {
                deleteMemberInfoRepository.delete(delete.get(i));
            }
        }
        System.out.println("삭제 완료");
    }
    @Scheduled(cron = "0 0 0 * * *")
    public void release() {
        List<BanMemberInfoEntity> banMember = banMemberInfoRepository.findAll();
        if(banMember.size() == 0) {
            System.out.println("정지 해제할 회원이 없습니다.");
            return;
        }
        for(int i=0; i<banMember.size(); i++) {
            if(banMember.get(i).getEndDt().equals(LocalDate.now())) {
                banMember.get(i).getMember().setStatus(1);
            }
        }
        System.out.println("정지 해제 완료");
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteImage() throws IOException {
        List<FileInfoEntity> file = fileRepository.findAll();
        for(int i=0; i<file.size(); i++) {
            if(file.get(i).getBoard() == null) {
                Path folderLocation = Paths.get(path);
                Path targetFile = folderLocation.resolve(file.get(i).getFilename());
                Files.delete(targetFile);
                fileRepository.delete(file.get(i));
            }
        }
        System.out.println("파일 삭제 완료");
    }
}
