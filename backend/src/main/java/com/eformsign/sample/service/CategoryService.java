package com.eformsign.sample.service;

import com.eformsign.sample.dto.TreeResponse;
import com.eformsign.sample.entity.Category;
import com.eformsign.sample.repository.CategoryClosureRepository;
import com.eformsign.sample.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryClosureRepository closureRepository;

    public List<TreeResponse> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll();

        // ID → Category 매핑
        Map<Long, Category> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // 부모 ID → 자식들
        Map<Long, List<Long>> parentToChildren = new HashMap<>();

        closureRepository.findAll().forEach(cc -> {
            if (cc.getDepth() == 1) {
                parentToChildren
                        .computeIfAbsent(cc.getAncestor(), k -> new ArrayList<>())
                        .add(cc.getDescendant());
            }
        });

        // 루트 노드 찾기: 자신이 조상이고 자신이 자손인 경우(depth = 0)만 있는 노드
        Set<Long> nonRoots = closureRepository.findAll().stream()
                .filter(cc -> cc.getDepth() == 1)
                .map(cc -> cc.getDescendant())
                .collect(Collectors.toSet());

        List<Long> rootIds = allCategories.stream()
                .map(Category::getId)
                .filter(id -> !nonRoots.contains(id))
                .collect(Collectors.toList());

        // 트리 구성
        List<TreeResponse> roots = new ArrayList<>();
        for (Long rootId : rootIds) {
            roots.add(buildTree(rootId, categoryMap, parentToChildren));
        }

        return roots;
    }

    private TreeResponse buildTree(Long id, Map<Long, Category> map, Map<Long, List<Long>> childrenMap) {
        TreeResponse node = new TreeResponse(id, map.get(id).getName(), new ArrayList<>());
        List<Long> children = childrenMap.getOrDefault(id, new ArrayList<>());
        for (Long childId : children) {
            node.addChild(buildTree(childId, map, childrenMap));
        }
        return node;
    }
}