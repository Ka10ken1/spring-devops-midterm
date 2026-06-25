package com.spring_midterm.midterm.util;

import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import com.spring_midterm.midterm.repository.IRepository;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OwnershipValidator {

    private OwnershipValidator() {}

    public static <TParent, TChild> TChild validate(
            IRepository<TParent> parentRepo, String parentName, Long parentId,
            IRepository<TChild> childRepo, String childName, Long childId,
            Function<TChild, Long> parentIdExtractor) {

        RepositoryUtils.findOrThrow(parentRepo, parentId, parentName);
        TChild child = RepositoryUtils.findOrThrow(childRepo, childId, childName);

        if (!parentIdExtractor.apply(child).equals(parentId)) {
            log.warn("{} with id {} not found for {} {}", childName, childId, parentName, parentId);
            throw new ResourceNotFoundException(childName, childId + " for " + parentName + " " + parentId);
        }
        return child;
    }
}
