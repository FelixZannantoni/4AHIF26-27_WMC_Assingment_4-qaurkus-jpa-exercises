package at.htl.catalogue;

import java.util.List;

public record Author(Long id, String name, List<String> titles) {
}
