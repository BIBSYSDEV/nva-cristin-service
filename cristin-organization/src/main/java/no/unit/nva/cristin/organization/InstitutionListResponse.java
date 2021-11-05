package no.unit.nva.cristin.organization;

import nva.commons.core.JacocoGenerated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class InstitutionListResponse implements List<InstitutionResponse> {

    private final List<InstitutionResponse> institutionResponseList;

    protected InstitutionListResponse() {
        institutionResponseList = new ArrayList<>();
    }

    public InstitutionListResponse(List<InstitutionResponse> institutionResponseList) {
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
    public Iterator<InstitutionResponse> iterator() {
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
    public boolean add(InstitutionResponse institutionResponse) {
        return institutionResponseList.add(institutionResponse);
    }

    @Override
    @JacocoGenerated
    public void add(int i, InstitutionResponse institutionResponse) {
        institutionResponseList.add(i, institutionResponse);
    }

    @Override
    @JacocoGenerated
    public boolean remove(Object o) {
        return institutionResponseList.remove(o);
    }

    @Override
    @JacocoGenerated
    public InstitutionResponse remove(int i) {
        return institutionResponseList.remove(i);
    }

    @Override
    @JacocoGenerated
    public boolean containsAll(Collection<?> collection) {
        return institutionResponseList.containsAll(collection);
    }

    @Override
    @JacocoGenerated
    public boolean addAll(Collection<? extends InstitutionResponse> collection) {
        return institutionResponseList.addAll(collection);
    }

    @Override
    @JacocoGenerated
    public boolean addAll(int i, Collection<? extends InstitutionResponse> collection) {
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
        if (!(o instanceof InstitutionListResponse)) {
            return false;
        }
        InstitutionListResponse that = (InstitutionListResponse) o;
        return Objects.equals(institutionResponseList, that.institutionResponseList);
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(institutionResponseList);
    }

    @Override
    @JacocoGenerated
    public InstitutionResponse get(int i) {
        return institutionResponseList.get(i);
    }

    @Override
    @JacocoGenerated
    public InstitutionResponse set(int i, InstitutionResponse institutionResponse) {
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
    public ListIterator<InstitutionResponse> listIterator() {
        return institutionResponseList.listIterator();
    }

    @Override
    @JacocoGenerated
    public ListIterator<InstitutionResponse> listIterator(int i) {
        return institutionResponseList.listIterator(i);
    }

    @Override
    @JacocoGenerated
    public List<InstitutionResponse> subList(int start, int end) {
        return institutionResponseList.subList(start, end);
    }
}
