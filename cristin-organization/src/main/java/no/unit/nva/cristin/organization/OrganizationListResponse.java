package no.unit.nva.cristin.organization;

import no.unit.nva.cristin.model.nva.Organization;
import nva.commons.core.JacocoGenerated;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class OrganizationListResponse implements List<Organization> {

    private final transient List<Organization> institutionResponseList;

    public OrganizationListResponse(List<Organization> institutionResponseList) {
        this.institutionResponseList = institutionResponseList;
    }

    @Override
    @JacocoGenerated
    public int size() {
        return institutionResponseList.size();
    }

    @Override
    @JacocoGenerated
    public boolean isEmpty() {
        return institutionResponseList.isEmpty();
    }

    @Override
    @JacocoGenerated
    public boolean contains(Object o) {
        return institutionResponseList.contains(o);
    }

    @Override
    @JacocoGenerated
    public Iterator<Organization> iterator() {
        return institutionResponseList.iterator();
    }

    @Override
    @JacocoGenerated
    public Object[] toArray() {
        return institutionResponseList.toArray();
    }

    @Override
    @JacocoGenerated
    public <T> T[] toArray(T[] a) {
        return institutionResponseList.toArray(a);
    }

    @Override
    @JacocoGenerated
    public boolean add(Organization institutionResponse) {
        return institutionResponseList.add(institutionResponse);
    }

    @Override
    @JacocoGenerated
    public void add(int i, Organization institutionResponse) {
        institutionResponseList.add(i, institutionResponse);
    }

    @Override
    @JacocoGenerated
    public boolean remove(Object o) {
        return institutionResponseList.remove(o);
    }

    @Override
    @JacocoGenerated
    public Organization remove(int i) {
        return institutionResponseList.remove(i);
    }

    @Override
    @JacocoGenerated
    public boolean containsAll(Collection<?> collection) {
        return institutionResponseList.containsAll(collection);
    }

    @Override
    @JacocoGenerated
    public boolean addAll(Collection<? extends Organization> collection) {
        return institutionResponseList.addAll(collection);
    }

    @Override
    @JacocoGenerated
    public boolean addAll(int i, Collection<? extends Organization> collection) {
        return institutionResponseList.addAll(i, collection);
    }

    @Override
    @JacocoGenerated
    public boolean removeAll(Collection<?> collection) {
        return institutionResponseList.removeAll(collection);
    }

    @Override
    @JacocoGenerated
    public boolean retainAll(Collection<?> collection) {
        return institutionResponseList.retainAll(collection);
    }

    @Override
    @JacocoGenerated
    public void clear() {
        institutionResponseList.clear();
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrganizationListResponse)) {
            return false;
        }
        OrganizationListResponse that = (OrganizationListResponse) o;
        return Objects.equals(institutionResponseList, that.institutionResponseList);
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(institutionResponseList);
    }

    @Override
    @JacocoGenerated
    public Organization get(int i) {
        return institutionResponseList.get(i);
    }

    @Override
    @JacocoGenerated
    public Organization set(int i, Organization institutionResponse) {
        return institutionResponseList.set(i, institutionResponse);
    }

    @Override
    @JacocoGenerated
    public int indexOf(Object o) {
        return institutionResponseList.indexOf(o);
    }

    @Override
    @JacocoGenerated
    public int lastIndexOf(Object o) {
        return institutionResponseList.lastIndexOf(o);
    }

    @Override
    @JacocoGenerated
    public ListIterator<Organization> listIterator() {
        return institutionResponseList.listIterator();
    }

    @Override
    @JacocoGenerated
    public ListIterator<Organization> listIterator(int i) {
        return institutionResponseList.listIterator(i);
    }

    @Override
    @JacocoGenerated
    public List<Organization> subList(int start, int end) {
        return institutionResponseList.subList(start, end);
    }
}
