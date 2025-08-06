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

    /**
     * 전체 카테고리를 트리 형태로 반환
     */
    public List<TreeResponse> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll();

        // ID → Category 매핑
        Map<Long, Category> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // 부모 ID → 자식 ID 리스트
        Map<Long, List<Long>> parentToChildren = new HashMap<>();

        closureRepository.findAll().forEach(cc -> {
            if (cc.getDepth() == 1) {
                parentToChildren
                        .computeIfAbsent(cc.getAncestor(), k -> new ArrayList<>())
                        .add(cc.getDescendant());
            }
        });

        // 루트 노드: 어떤 노드의 자식으로도 안 들어간 노드
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

    /**
     * 특정 depth2 카테고리 하위의 depth3 카테고리 ID 목록 반환
     */
    public List<Long> getDepth3CategoryIdsUnder(Long depth2Id) {
        return closureRepository.findAll().stream()
                .filter(cc -> cc.getAncestor().equals(depth2Id) && cc.getDepth() == 1)
                .map(cc -> cc.getDescendant())
                .collect(Collectors.toList());
    }

    /**
     * 주어진 카테고리의 전체 상위 경로 이름 리스트 반환 (depth3 → depth2 → depth1)
     */
    public List<String> getCategoryPath(Long categoryId) {
        // 카테고리 경로를 구할 때, 자신 포함 모든 조상을 찾는다 (자기 자신도 포함, depth 오름차순)
        List<Long> ancestorIds = closureRepository.findAll().stream()
                .filter(cc -> cc.getDescendant().equals(categoryId))
                .sorted(Comparator.comparingInt(cc -> cc.getDepth())) // depth 기준 정렬
                .map(cc -> cc.getAncestor())
                .collect(Collectors.toList());

        // 이름으로 변환
        List<Category> categories = categoryRepository.findAllById(ancestorIds);
        Map<Long, String> nameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 자손 → 조상 순서로 뒤집어서 리턴 (하위 > 상위)
        Collections.reverse(ancestorIds);

        return ancestorIds.stream()
                .map(nameMap::get)
                .collect(Collectors.toList());
    }
}