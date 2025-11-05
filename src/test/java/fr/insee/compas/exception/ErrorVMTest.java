package fr.insee.compas.exception;

import static org.assertj.core.api.Assertions.*;

import java.io.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorVMTest {

    @Test
    @DisplayName("Constante d'erreur exposée")
    void constant_isExposed() {
        assertThat(ErrorVM.ERR_INTERNAL_SERVER_ERROR).isEqualTo("error.internalServerError");
    }

    @Test
    @DisplayName("Getters/Setters basiques")
    void gettersSetters() {
        ErrorVM vm = new ErrorVM();
        vm.setCle("CLE");
        vm.setMessage("MSG");

        assertThat(vm.getCle()).isEqualTo("CLE");
        assertThat(vm.getMessage()).isEqualTo("MSG");
    }

    @Test
    @DisplayName("equals/hashCode: deux instances vierges sont égales")
    void equals_hashCode_defaultInstances() {
        ErrorVM a = new ErrorVM();
        ErrorVM b = new ErrorVM();

        assertThat(a)
                .isEqualTo(b) // deux instances identiques
                .hasSameHashCodeAs(b) // même hashCode
                .isEqualTo(a) // réflexivité
                .isNotEqualTo(null) // pas égal à null
                .isNotEqualTo("x"); // pas égal à une autre classe
    }

    @Test
    @DisplayName("equals/hashCode: champs identiques => égalité et hash identique")
    void equals_hashCode_sameFields() {
        ErrorVM a = new ErrorVM();
        a.setCle("A");
        a.setMessage("M");

        ErrorVM b = new ErrorVM();
        b.setCle("A");
        b.setMessage("M");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("equals: clé différente => non égal")
    void equals_differentKey() {
        ErrorVM a = new ErrorVM();
        a.setCle("A");
        ErrorVM b = new ErrorVM();
        b.setCle("B");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("equals: message différent => non égal")
    void equals_differentMessage() {
        ErrorVM a = new ErrorVM();
        a.setCle("A");
        a.setMessage("M1");

        ErrorVM b = new ErrorVM();
        b.setCle("A");
        b.setMessage("M2");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("equals: nullité des champs prise en compte (null vs non-null)")
    void equals_nullVsNonNull() {
        ErrorVM a = new ErrorVM();
        a.setCle(null);
        a.setMessage("M");

        ErrorVM b = new ErrorVM();
        b.setCle("A");
        b.setMessage("M");

        assertThat(a).isNotEqualTo(b);

        // même logique pour message
        ErrorVM c = new ErrorVM();
        c.setCle("A");
        c.setMessage(null);
        ErrorVM d = new ErrorVM();
        d.setCle("A");
        d.setMessage("M");
        assertThat(c).isNotEqualTo(d);
    }

    @Test
    @DisplayName("Sérialisation: round-trip conserve l'égalité")
    void serialization_roundTrip() throws Exception {
        ErrorVM src = new ErrorVM();
        src.setCle("ERR");
        src.setMessage("Oups");

        assertThat(src).isInstanceOf(Serializable.class);

        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(src);
            oos.flush();
            bytes = bos.toByteArray();
        }

        ErrorVM restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            restored = (ErrorVM) ois.readObject();
        }

        assertThat(restored).isEqualTo(src);
        assertThat(restored.getCle()).isEqualTo("ERR");
        assertThat(restored.getMessage()).isEqualTo("Oups");
    }
}
